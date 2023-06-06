package no.nav.arbeidsplassen.puls.batch

import io.micrometer.core.instrument.MeterRegistry
import io.micronaut.aop.Around
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.arbeidsplassen.puls.amplitude.AmplitudeClient
import no.nav.arbeidsplassen.puls.amplitude.AmplitudeParser
import no.nav.arbeidsplassen.puls.event.PulsEventTotalService
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.HOURS
import java.util.zip.GZIPInputStream
import java.util.zip.ZipFile
import javax.transaction.Transactional

@Singleton
@Around
class BatchFetchAmplitudeExport(
    private val client: AmplitudeClient, private val amplitudeParser: AmplitudeParser,
    private val pulsEventTotalService: PulsEventTotalService, private val batchRunRepository: BatchRunRepository,
    private val meterRegistry: MeterRegistry
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(BatchFetchAmplitudeExport::class.java)
    }

    fun startBatchRunFetchExports(
        fetchFrom: Instant = Instant.now().minus(2, HOURS),
        fetchTo: Instant = fetchFrom
    ): BatchRun {
        val exportInfo = prepareExportInfo(fetchFrom, fetchTo)
        LOG.info("Will run batch from ${exportInfo.startTime} to ${exportInfo.endTime}")
        return batchRunRepository.startTimeIntersectInterval(exportInfo.startTime)?.let {
            LOG.info("This start time intersect with a batch that already exist in database, skipping run")
            it
        } ?: kotlin.run {
            LOG.info("Running batch ${exportInfo.batchRunName}")
            fetchAmplitudeExport(exportInfo)
            crunchAmplitudeExportData(exportInfo)
        }
    }

    private fun prepareExportInfo(fetchFrom: Instant, fetchTo: Instant): AmplitudeExportInfo {
        val batchName = "amplitude-${fetchFrom.toAmplitudeString()}-${fetchTo.toAmplitudeString()}"
        return AmplitudeExportInfo(
            File("/tmp/${batchName}.zip"),
            batchName,
            fetchFrom.truncatedTo(HOURS),
            fetchTo.truncatedTo(HOURS)
        )
    }

    @Transactional
    fun crunchAmplitudeExportData(exportInfo: AmplitudeExportInfo): BatchRun {
        val batchRun = batchRunRepository.save(
            BatchRun(
                name = exportInfo.batchRunName,
                startTime = exportInfo.startTime, endTime = exportInfo.endTime
            )
        )
        val unzippedFiles = unzipExportFile(exportInfo.tmpFile)
        var totalEvents = 0
        unzippedFiles.forEach {
            val events = amplitudeParser.calculateAmplitudeClickEvents(it)
            pulsEventTotalService.updatePulsEventTotal(events)
            totalEvents = +events.size
        }
        if (totalEvents <= 0) LOG.error("Batch run ${exportInfo.batchRunName} did not produced any events size: $totalEvents")
        else LOG.info("Batch ${exportInfo.batchRunName} run successfully with totalEvents $totalEvents")
        meterRegistry.counter("batch_run", "name", "amplitude").increment(totalEvents.toDouble())
        unzippedFiles.forEach { File(it).delete() }
        exportInfo.tmpFile.delete()
        return batchRunRepository.save(batchRun.copy(status = BatchRunStatus.DONE, totalEvents = totalEvents))
    }

    private fun fetchAmplitudeExport(exportInfo: AmplitudeExportInfo) {
        val startTime = exportInfo.startTime.toAmplitudeString()
        val endTime = exportInfo.endTime.toAmplitudeString()
        LOG.info("Fetching amplitude export from $startTime to $endTime")
        LOG.info("writing to ${exportInfo.tmpFile.name}")
        runBlocking {
            val exportsResponse = client.fetchExports(startTime, endTime)
            if (exportsResponse.statusCode() == 404) {
                LOG.info("Amplitude returned empty export data (404) for $startTime to $endTime")
            } else if (exportsResponse.statusCode() in 200..299
                && exportsResponse.body() != null) {
                exportInfo.tmpFile.outputStream().use { it.write(exportsResponse.body()) }
            } else {
                LOG.error("Amplitude returned statuscode ${exportsResponse.statusCode()} for export from $startTime to $endTime")
            }
        }
    }

    private fun unzipExportFile(exportfile: File): List<String> {
        val files = mutableListOf<String>()
        ZipFile(exportfile).use { zip ->
            LOG.info("got zip file ${zip.name}")
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    LOG.info("unpacking $entry")
                    GZIPInputStream(input).use { gzinput ->
                        val jsonFile = "/tmp"+entry.name.substring(entry.name.indexOf("/"),entry.name.lastIndexOf("."))
                        LOG.info("writing $jsonFile")
                        File(jsonFile).outputStream().use {
                            gzinput.copyTo(it)
                        }
                        files.add(jsonFile)
                    }
                }
            }
        }
        return files
    }

}

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HH").withZone(ZoneOffset.UTC)
fun Instant.toAmplitudeString(): String = formatter.format(this)
fun String.toInstant(): Instant = Instant.from(formatter.parse(this))
