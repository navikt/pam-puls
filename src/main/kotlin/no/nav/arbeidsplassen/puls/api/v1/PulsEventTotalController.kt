package no.nav.arbeidsplassen.puls.api.v1

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
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

}
