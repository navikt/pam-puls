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

    private val daysOld: Long = 14
    private var kafkaHasError = false

    companion object {
        private val LOG = LoggerFactory.getLogger(OutboxScheduler::class.java)
    }

    @Scheduled(fixedDelay = "1m")
    fun outboxToKafka() {
        if (election.isLeader() && kafkaHasError.not()) {
            repository.findByStatusOrderByUpdated(OutboxStatus.PENDING, Pageable.from(0, 100)).forEach { outbox ->
                val key = "${outbox.payload.oid}#${outbox.payload.type}"
                kafkaSender.sendPulsEvent(key, outbox.payload).subscribe(
                    {
                        repository.save(outbox.copy(status = OutboxStatus.DONE))
                        LOG.debug("sent successfully $key")
                    },
                    {
                        LOG.error("Got error while sending to kafka, will stop sending", it)
                        repository.save(outbox.copy(status = OutboxStatus.ERROR))
                        kafkaHasError = true
                    }
                )
            }
        }
        else if (kafkaHasError) LOG.error("Kafka is error state!")
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
