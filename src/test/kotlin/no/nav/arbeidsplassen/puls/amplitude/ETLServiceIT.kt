package no.nav.arbeidsplassen.puls.amplitude

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.puls.ETLService
import org.junit.jupiter.api.Test


@MicronautTest
class ETLServiceIT(private val etlService: ETLService) {


    @Test
    fun startService() {
        val files = etlService.processAmplideExports()
    }
}
