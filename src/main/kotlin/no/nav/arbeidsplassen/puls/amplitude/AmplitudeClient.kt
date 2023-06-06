package no.nav.arbeidsplassen.puls.amplitude

import io.micronaut.http.HttpHeaders.ACCEPT
import io.micronaut.http.HttpHeaders.USER_AGENT
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Headers
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.net.http.HttpResponse

@Client("\${amplitude.proxy_base_url}")
@Headers(Header(name = USER_AGENT, value = "Micronaut HTTP Client"))
interface AmplitudeClient {
    @Get("/export")
    @Header(name = ACCEPT, value = "application/zip")
    suspend fun fetchExports(@QueryValue("start") start: String, @QueryValue("end") end: String): HttpResponse<ByteArray>
}
