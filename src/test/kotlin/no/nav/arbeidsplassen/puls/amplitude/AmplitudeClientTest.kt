package no.nav.arbeidsplassen.puls.amplitude

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.io.File

@MicronautTest
class AmplitudeClientTest(private val client: AmplitudeClient) {

    //@Test Good for testing integration against ampltiude
    fun fetchExport() {
        runBlocking {
            val tmpFile = File("/tmp/amplitude.zip")
            tmpFile.outputStream().use { it.write(client.fetchExports("20220228T00","20220228T23")) }
        }
    }
}
