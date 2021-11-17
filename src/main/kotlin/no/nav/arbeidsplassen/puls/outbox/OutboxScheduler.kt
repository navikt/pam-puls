package no.nav.arbeidsplassen.puls.outbox

import io.micronaut.data.model.Pageable
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import no.nav.arbeidsplassen.puls.LeaderElection
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.temporal.ChronoUnit

@Singleton
class OutboxScheduler(private val repository: OutboxRepository, private val election: LeaderElection,
                      private val kafkaSender: OutboxKafkaSender) {

    private val daysOld: Long = 7
    private var kafkaHasError = false

    companion object {
        private val LOG = LoggerFactory.getLogger(OutboxScheduler::class.java)
    }

    @Scheduled(fixedDelay = "10s")
    fun outboxToKafka() {
        if (election.isLeader() && kafkaHasError.not()) {
            var ok = 0
            var error = 0
            repository.findByStatusOrderByUpdated(OutboxStatus.PENDING, Pageable.from(0, 200)).forEach { outbox ->
                val key = outbox.payload.oid
                kafkaSender.sendPulsEvent(key, outbox.payload).subscribe(
                    {
                        LOG.info("Successfully sent to kafka event with key: $key")
                        repository.save(outbox.copy(status = OutboxStatus.DONE))
                        ok++
                    },
                    {
                        LOG.error("Got error while sending to kafka, will stop sending", it)
                        repository.save(outbox.copy(status = OutboxStatus.ERROR))
                        kafkaHasError = true
                    }
                )
            }
            if (ok>0) LOG.info("$ok pulsevents was sent to kafka")
            if (error>0) LOG.error("We got $error while trying to send to kafka")
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
