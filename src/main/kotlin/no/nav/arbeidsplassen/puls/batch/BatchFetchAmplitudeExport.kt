package no.nav.arbeidsplassen.puls.batch

import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.arbeidsplassen.puls.amplitude.AmplitudeClient
import no.nav.arbeidsplassen.puls.amplitude.AmplitudeParser
import no.nav.arbeidsplassen.puls.event.PulsEventTotalService
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.zip.GZIPInputStream
import java.util.zip.ZipFile


@Singleton
class BatchFetchAmplitudeExport(private val client: AmplitudeClient, private val amplitudeParser: AmplitudeParser,
                                private val pulsEventTotalService: PulsEventTotalService, private val batchRunRepository: BatchRunRepository) {

    private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HH").withZone(ZoneId.from(ZoneOffset.UTC))

    companion object {
        private val LOG = LoggerFactory.getLogger(BatchFetchAmplitudeExport::class.java)
    }

    fun processAmplitudeExports() {
        val fetchFrom = batchRunRepository.findMaxId()?.let {
            batchRunRepository.findById(it).get().endTime.plus(1,ChronoUnit.HOURS)
        } ?: run {
            Instant.now().minus(2, ChronoUnit.HOURS)
        }
        val exportInfo = fetchAmplitudeExport(fetchFrom)
        batchRunRepository.findByName(exportInfo.batchRunName)?.let {
            LOG.warn("${it.name} already exist in database last updated ${it.updated}, skipping run")
        } ?: run {
            crunchAmplitudeExportData(exportInfo)
        }
        exportInfo.tmpFile.delete()
    }

    private fun crunchAmplitudeExportData(exportInfo: AmplitudeExportInfo) {
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
        batchRunRepository.save(batchRun.copy(status = BatchRunStatus.DONE, totalEvents = totalEvents))
        unzippedFiles.forEach { File(it).delete() }
    }

    fun fetchAmplitudeExport(startTime: Instant = Instant.now().minus(2, ChronoUnit.HOURS),
                             endTime: Instant = startTime): AmplitudeExportInfo {
        val startstr = formatter.format(startTime)
        val endstr = formatter.format(endTime)
        LOG.info("Fetching amplitude export from $startstr to $endstr")
        val batchName = "amplitude-$startstr-$endstr"
        val tmpFile = File("/tmp/$batchName.zip")
        runBlocking {
            tmpFile.outputStream().use { it.write(client.fetchExports(startstr, endstr)) }
        }
        return AmplitudeExportInfo(tmpFile, batchName, tmpFile.length(),startTime.truncatedTo(ChronoUnit.HOURS), endTime.truncatedTo(ChronoUnit.HOURS))
    }

    fun unzipExportFile(exportfile: File):List<String>  {
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

