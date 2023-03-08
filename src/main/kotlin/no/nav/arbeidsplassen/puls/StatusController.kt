package no.nav.arbeidsplassen.puls

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/internal")
class StatusController {
    @Get("/isReady")
    fun isReady(): String {
        return "OK"
    }

    @Get("/isAlive")
    fun isAlive(): HttpResponse<String> {
        return HttpResponse.ok("OK")
    }
}
