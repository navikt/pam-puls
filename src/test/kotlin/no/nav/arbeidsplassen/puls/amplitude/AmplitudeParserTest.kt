package no.nav.arbeidsplassen.puls.amplitude

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test

@MicronautTest
class AmplitudeParserTest(val objectMapper: ObjectMapper, val amplitudeParser: AmplitudeParser) {

    @Test
    fun parseAmplitude() {
        val events = amplitudeParser.calculateAmplitudeClickEvents("./src/test/resources/amplitude.json")
        println("TOTAL: ${events.size}")
        events.forEach { println("uuid: ${it.oId} type: ${it.type} title: ${it.title} total: ${it.total}")}
    }
}

