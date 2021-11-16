package no.nav.arbeidsplassen.puls.batch

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
class BatchFetchAmplitudeExport(private val client: AmplitudeClient, private val amplitudeParser: AmplitudeParser,
                                private val pulsEventTotalService: PulsEventTotalService, private val batchRunRepository: BatchRunRepository) {


    companion object {
        private val LOG = LoggerFactory.getLogger(BatchFetchAmplitudeExport::class.java)
    }

    fun startBatchRunFetchExports(fetchFrom: Instant = Instant.now().minus(2, HOURS), fetchTo: Instant = fetchFrom): BatchRun {
        LOG.info("Fetch from is $fetchFrom")
        val exportInfo = prepareExportInfo(fetchFrom, fetchTo)
        return batchRunRepository.findByName(exportInfo.batchRunName)?.let {
            LOG.warn("${it.name} already exist in database last updated ${it.updated}, skipping run")
            it
        } ?: run {
            LOG.info("Running batch ${exportInfo.batchRunName}")
            fetchAmplitudeExport(exportInfo)
            crunchAmplitudeExportData(exportInfo)
        }
    }

    private fun prepareExportInfo(fetchFrom: Instant, fetchTo: Instant): AmplitudeExportInfo {
        val batchName = "amplitude-${fetchFrom.toAmplitudeString()}-${fetchTo.toAmplitudeString()}"
        return AmplitudeExportInfo(File("/tmp/${batchName}.zip"), batchName, fetchFrom.truncatedTo(HOURS) ,fetchTo.truncatedTo(HOURS))
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
        LOG.info("Batch ${exportInfo.batchRunName} run successfully with totalEvents $totalEvents")
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
            exportInfo.tmpFile.outputStream().use { it.write(client.fetchExports(startTime,endTime)) }
        }
    }

    private fun unzipExportFile(exportfile: File):List<String>  {
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

