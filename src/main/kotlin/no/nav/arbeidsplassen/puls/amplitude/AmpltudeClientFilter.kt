package no.nav.arbeidsplassen.puls.amplitude

import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.ClientFilterChain
import io.micronaut.http.filter.HttpClientFilter
import org.reactivestreams.Publisher

@Filter("/api/2/**")
class AmpltudeClientFilter(private val config: AmplitudeConfig) : HttpClientFilter {

    override fun doFilter(request: MutableHttpRequest<*>, chain: ClientFilterChain): Publisher<out HttpResponse<*>?> {
        return chain.proceed(request.basicAuth(config.apikey, config.secret))
    }
}
