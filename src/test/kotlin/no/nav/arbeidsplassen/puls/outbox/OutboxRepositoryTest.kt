package no.nav.arbeidsplassen.puls.outbox

import io.micronaut.data.model.Pageable
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.arbeidsplassen.puls.event.PulsEventTotalDTO
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@MicronautTest
class OutboxRepositoryTest(private val outboxRepository: OutboxRepository) {

    @Test
    fun saveAndReadOutbox() {
        val outbox = Outbox(oid="1234567", payload = PulsEventTotalDTO(oid="1234567", total = 1, type = "Stillings visning"))
        val saved = outboxRepository.save(outbox)
        assertNotNull(saved.id)
        val exists = outboxRepository.findByStatusOrderByUpdated(OutboxStatus.PENDING, Pageable.from(0,100))
        assertTrue(exists.isNotEmpty())
        val updated = outboxRepository.save(saved.copy(status = OutboxStatus.DONE))
        val db = outboxRepository.findById(updated.id!!).get()
        assertEquals(db.status, OutboxStatus.DONE)

    }
}
