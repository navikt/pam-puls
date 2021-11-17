package no.nav.arbeidsplassen.puls.outbox

import io.micronaut.data.model.Pageable
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.puls.LeaderElection
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit

@Singleton
class OutboxScheduler(private val repository: OutboxRepository, private val election: LeaderElection) {

    private val daysOld: Long = 7

    companion object {
        private val LOG = LoggerFactory.getLogger(OutboxScheduler::class.java)
    }

    @Scheduled(fixedDelay = "10s")
    fun outboxToKafka() {
        if (election.isLeader()) {
            var count = 0
            repository.findByStatusOrderByUpdated(OutboxStatus.PENDING, Pageable.from(0, 200)).forEach {
                //TODO send to kafka here
                repository.save(it.copy(status = OutboxStatus.DONE))
                count++
            }
            if (count>0) LOG.info("$count pulsevents was sent to kafka")
        }
    }

    @Scheduled(cron = "0 0 8 * * *")
    fun cleanOldEvents() {
        if (election.isLeader()) {
            val old = Instant.now().minus(daysOld,ChronoUnit.DAYS)
            val deleted = repository.deleteByStatusAndUpdatedBefore(OutboxStatus.DONE, old)
            LOG.info("total $deleted old events from outbox was deleted")
        }
    }

}
