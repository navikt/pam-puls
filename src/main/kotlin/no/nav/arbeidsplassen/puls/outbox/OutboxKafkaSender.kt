package no.nav.arbeidsplassen.puls.outbox

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.Topic
import no.nav.arbeidsplassen.puls.event.PulsEventTotalDTO
import org.apache.kafka.clients.producer.RecordMetadata
import reactor.core.publisher.Flux

@KafkaClient
interface OutboxKafkaSender {

    @KafkaClient(batch = true)
    @Topic("pam-puls-intern-2")
    fun sendPulsEvent(@KafkaKey key: String, pulsevent: PulsEventTotalDTO): Flux<RecordMetadata>

}
