package no.nav.arbeidsplassen.puls

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.arbeidsplassen.puls.amplitude.AmplitudeClient
import no.nav.arbeidsplassen.puls.amplitude.AmplitudeParser
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.GZIPInputStream
import java.util.zip.ZipFile


@Singleton
class ETLService(private val objectMapper: ObjectMapper, private val client: AmplitudeClient, val amplitudeParser: AmplitudeParser) {

    private val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HH")

    companion object {
        private val LOG = LoggerFactory.getLogger(ETLService::class.java)
    }

    fun processAmplideExports() {
        unzipExportFile(fetchAmplitudeExport()).forEach {
            val events = amplitudeParser.calculateAmplitudeClickEvents(it)
            LOG.info("We got ${events.size}")
        }
    }

    fun fetchAmplitudeExport(startTime:LocalDateTime = LocalDateTime.now().minusHours(3), endTime: LocalDateTime = startTime.plusHours(1)): String {
        val startstr = formatter.format(startTime)
        val endstr = formatter.format(endTime)
        LOG.info("Fetching amplitude export from $startstr to $endstr")
        val tmpFileName = "/tmp/export_$startstr-$endstr.zip"
        runBlocking {
            File(tmpFileName).outputStream().use { it.write(client.fetchExports(startstr, endstr)) }
        }
        return tmpFileName
    }

    fun unzipExportFile(exportfile: String):List<String>  {
        val files = mutableListOf<String>()
        ZipFile(File(exportfile)).use { zip ->
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

