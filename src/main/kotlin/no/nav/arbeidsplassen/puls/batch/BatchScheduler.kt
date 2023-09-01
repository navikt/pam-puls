package no.nav.arbeidsplassen.puls.batch

import io.micrometer.core.instrument.MeterRegistry
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.puls.LeaderElection
import org.slf4j.LoggerFactory

@Singleton
class BatchScheduler(
    private val batchFetchAmplitudeExport: BatchFetchAmplitudeExport,
    private val leaderElection: LeaderElection,
    private val meterRegistry: MeterRegistry
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(BatchScheduler::class.java)
    }

    private val batchRunSuccess = meterRegistry.counter("pam.puls.batch.success")

    @Scheduled(fixedDelay = "16m")
    fun startFetchAmplitudeAndProcessEvents() {
        if (leaderElection.isLeader()) {
            LOG.info("Running fetch export from amplitude")
            val time = System.currentTimeMillis()
            batchFetchAmplitudeExport.startBatchRunFetchExports()
            LOG.info("Batch was finished in ${(System.currentTimeMillis() - time) / 1000}s")
            batchRunSuccess.increment()
        }
    }
}
