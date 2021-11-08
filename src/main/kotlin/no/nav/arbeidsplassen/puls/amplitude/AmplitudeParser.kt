package no.nav.arbeidsplassen.puls.amplitude

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.puls.event.PulsEventTotal
import org.slf4j.LoggerFactory
import java.io.File

@Singleton
class AmplitudeParser(private val objectMapper: ObjectMapper) {

    private val factory = JsonFactory()
    companion object {
        private final val LOG = LoggerFactory.getLogger(AmplitudeParser::class.java)
    }

    fun calculateAmplitudeClickEvents(amplitudeFile: String) : List<PulsEventTotal> {
        val parser = factory.createParser(File(amplitudeFile))
        val amplitudeEvents = ArrayList<AmplitudeEvent>()
        while (parser.nextToken()!=null) {
            val event  = objectMapper.readValue(parser, AmplitudeEvent::class.java)
            if (event.event_properties.id!=null) {
                amplitudeEvents.add(event)
            }
        }
        LOG.info("A total of {} events with ID was found", amplitudeEvents.size)
        return processPulsEventTotals(amplitudeEvents = amplitudeEvents)
    }

    fun processPulsEventTotals(amplitudeEvents: List<AmplitudeEvent>): List<PulsEventTotal> {
        val eventsMap = HashMap<String, PulsEventTotal>()
        amplitudeEvents.forEach { aev ->
            val key = aev.event_properties.id+aev.event_type
            if (eventsMap[key]!=null) {
                val current = eventsMap[key]!!
                eventsMap[key] = PulsEventTotal(oId=current.oId, type = current.type, total = current.total+1, title = aev.event_properties.title, id = current.id)
            }
            else {
                eventsMap[key] = PulsEventTotal(oId=aev.event_properties.id, type = aev.event_type, title = aev.event_properties.title)
            }
        }
        return eventsMap.values.toList()
    }
}

data class AmplitudeEvent(val event_type: String, val event_properties: EventProperty)
class EventProperty(val id: String?, val title: String?)
