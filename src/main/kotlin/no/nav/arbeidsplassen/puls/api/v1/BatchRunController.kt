package no.nav.arbeidsplassen.puls.api.v1

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import no.nav.arbeidsplassen.puls.batch.BatchFetchAmplitudeExport
import no.nav.arbeidsplassen.puls.batch.BatchRun
import no.nav.arbeidsplassen.puls.batch.BatchRunRepository
import no.nav.arbeidsplassen.puls.batch.toInstant
import org.slf4j.LoggerFactory

@Controller("/api/v1/batchruns")
class BatchRunController(private val batchRunRepository: BatchRunRepository,
                         private val batchFetchAmplitudeExport: BatchFetchAmplitudeExport) {

    companion object {
        private val LOG = LoggerFactory.getLogger(BatchRunController::class.java)
    }

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

    @Post("/start")
    fun startFrom(@QueryValue from: String, @QueryValue toOptional: String?): BatchRun {
        val to = toOptional ?: from
        LOG.info("Request to manually fetch from $from to $to")
        return batchFetchAmplitudeExport.startBatchRunFetchExports(from.toInstant(), to.toInstant())
    }
}
