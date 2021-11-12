package no.nav.arbeidsplassen.puls.batch

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.puls.batch.BatchFetchAmplitudeExport
import org.junit.jupiter.api.Test


@MicronautTest
class BatchFetchAmplitudeExportTest(private val batchFetchAmplitudeExport: BatchFetchAmplitudeExport) {


    //@Test ignore, this test is useful only in localhost
    fun startService() {
        batchFetchAmplitudeExport.startBatchRunFetchExports()
    }
}
