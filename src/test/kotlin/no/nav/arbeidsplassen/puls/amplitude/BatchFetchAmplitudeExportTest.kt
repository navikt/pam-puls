package no.nav.arbeidsplassen.puls.amplitude

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.puls.batch.BatchFetchAmplitudeExport


@MicronautTest
class BatchFetchAmplitudeExportTest(private val batchFetchAmplitudeExport: BatchFetchAmplitudeExport) {


    //@Test
    fun startService() {
        batchFetchAmplitudeExport.processAmplitudeExports()
    }
}
