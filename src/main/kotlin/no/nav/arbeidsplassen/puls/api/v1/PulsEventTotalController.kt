package no.nav.arbeidsplassen.puls.api.v1

import io.micronaut.context.annotation.Parameter
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import no.nav.arbeidsplassen.puls.amplitude.EVENT_TYPES
import no.nav.arbeidsplassen.puls.event.PulsEventTotalDTO
import no.nav.arbeidsplassen.puls.event.PulsEventTotalService

@Controller("/api/v1/events")
class PulsEventTotalController(private val pulsEventTotalService: PulsEventTotalService) {

    @Get("/{oid}/{type}")
    fun fetchPulsEventTotalByOidAndType(oid: String, type: String): PulsEventTotalDTO? {
        return pulsEventTotalService.findByOidAndType(oid, type)
    }

    @Get("/{oid}")
    fun fetchPulsEventsTotalByOid(oid:String): List<PulsEventTotalDTO> {
        return pulsEventTotalService.findByOid(oid)
    }

    @Get("/top/{type}/{number}")
    fun fetchTopByNumber(@PathVariable type: String, @PathVariable number: Int): List<PulsEventTotalDTO> {
        return if (EVENT_TYPES.contains(type)) pulsEventTotalService.findPulsEventTotalByTypeAndTopByNumber(type, number)
            else emptyList()
    }
}
