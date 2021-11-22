package no.nav.arbeidsplassen.puls.amplitude

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.puls.event.PulsEventTotalService
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@MicronautTest
class AmplitudeToPulsEventsTest(private val objectMapper: ObjectMapper, private val amplitudeParser: AmplitudeParser,
                                private val pulsEventTotalService: PulsEventTotalService) {

    @Test
    fun parseAmplitude() {
        val events = amplitudeParser.calculateAmplitudeClickEvents("./src/test/resources/amplitude.json")
        pulsEventTotalService.updatePulsEventTotal(events)
        val event = pulsEventTotalService.findByOidAndType("d3d1a015-ef74-47bd-9122-ad5037d04d3d", "Stilling visning")
        assertNotNull(event)
        println(objectMapper.writeValueAsString(event))
    }
}

