package no.nav.arbeidsplassen.puls.amplitude

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.puls.event.PulsEventTotalDTO
import org.slf4j.LoggerFactory
import java.io.File

@Singleton
class AmplitudeParser(private val objectMapper: ObjectMapper) {


    private val factory = JsonFactory()

    companion object {
        private val LOG = LoggerFactory.getLogger(AmplitudeParser::class.java)
    }

    fun calculateAmplitudeClickEvents(amplitudeFile: String): List<PulsEventTotalDTO> {
        val parser = factory.createParser(File(amplitudeFile))
        val amplitudeEvents = ArrayList<AmplitudeEvent>()
        while (parser.nextToken() != null) {
            val event = objectMapper.readValue(parser, AmplitudeEvent::class.java)
            if (eventHasIdAndKnownType(event)) {
                amplitudeEvents.add(event)
            }
        }
        LOG.info("A total of {} events with ID was found", amplitudeEvents.size)
        return calculatePulsEventTotals(amplitudeEvents = amplitudeEvents)
    }

    private fun calculatePulsEventTotals(amplitudeEvents: List<AmplitudeEvent>): List<PulsEventTotalDTO> {
        val eventsMap = HashMap<String, PulsEventTotalDTO>()
        amplitudeEvents.forEach { aev ->
            val eventId = aev.event_properties["id"].toString()
            val key = eventId + aev.event_type
            if (eventsMap[key] != null) {
                val current = eventsMap[key]!!
                eventsMap[key] = PulsEventTotalDTO(
                    oid = eventId,
                    type = aev.event_type,
                    total = current.total + 1,
                    properties = aev.event_properties
                )
            } else {
                eventsMap[key] =
                    PulsEventTotalDTO(oid = eventId, type = aev.event_type, properties = aev.event_properties)
            }
        }
        return eventsMap.values.toList()
    }

    private fun eventHasIdAndKnownType(aev: AmplitudeEvent) =
        aev.event_properties["id"] != null && EVENT_TYPES.contains(aev.event_type)
}

data class AmplitudeEvent(val event_type: String, val event_properties: HashMap<String, Any>)
val EVENT_TYPES = listOf("Stilling visning", "Stilling sok-via-url")
