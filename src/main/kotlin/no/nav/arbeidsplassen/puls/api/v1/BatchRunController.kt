package no.nav.arbeidsplassen.puls.api.v1

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import no.nav.arbeidsplassen.puls.batch.BatchRun
import no.nav.arbeidsplassen.puls.batch.BatchRunRepository

@Controller("/api/v1/batchruns")
class BatchRunController(private val batchRunRepository: BatchRunRepository) {

    @Get("/name/{name}")
    fun fetchBatchRunByName(name:String): BatchRun? {
        return batchRunRepository.findByName(name)
    }

    @Get("/last/batch")
    fun fetchLastBatchRun(): BatchRun? {
        return batchRunRepository.findMaxId()?.let {
            batchRunRepository.findById(it).get()
        }
    }
}
