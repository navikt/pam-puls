package no.nav.arbeidsplassen.puls.event

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Instant

@MicronautTest
class PulsEventTotalDTOWriterTest(private val objectMapper: ObjectMapper) {


    @Test
    fun writeDTOForContract() {
        val puls = PulsEventTotalDTO(id=1, oid="175c3b13-6b56-4cc6-82d3-fb02089e6726", total = 10, type = "Stilling visning",
            created = Instant.MAX, updated = Instant.MAX, properties = hashMapOf(Pair("title", "This is a title")))
        val jsonFile = File("src/test/resources/dto/pulseventtotal.json")
        jsonFile.writeText(objectMapper.writeValueAsString(puls))
    }
}
