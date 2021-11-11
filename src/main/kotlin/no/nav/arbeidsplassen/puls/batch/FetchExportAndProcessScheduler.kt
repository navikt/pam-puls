package no.nav.arbeidsplassen.puls.batch

import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.puls.LeaderElection
import org.slf4j.LoggerFactory

@Singleton
class FetchExportAndProcessScheduler(private val batchFetchAmplitudeExport: BatchFetchAmplitudeExport,
                                     private val leaderElection: LeaderElection) {

    companion object {
        private val LOG = LoggerFactory.getLogger(FetchExportAndProcessScheduler::class.java)
    }

    @Scheduled(cron="* 5 * * * *")
    fun startFetchAmplitudeAndProcessEvents() {
        if (leaderElection.isLeader()) {
            LOG.info("Running fetch export from amplitude")
        }
    }

}
