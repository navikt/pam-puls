package no.nav.arbeidsplassen.puls.amplitude


import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Requires

@ConfigurationProperties(AmplitudeConfig.PREFIX)
@Requires(property = AmplitudeConfig.PREFIX)
class AmplitudeConfig {

    var apikey: String? = null
    var secret: String? = null

    companion object {
        const val PREFIX = "amplitude"
        const val API_URL = "https://amplitude.com/api/2"
    }

}
