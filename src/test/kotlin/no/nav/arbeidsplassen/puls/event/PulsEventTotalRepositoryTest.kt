package no.nav.arbeidsplassen.puls.event

import io.micronaut.data.model.Pageable
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class PulsEventTotalRepositoryTest(private val pulsEventTotalRepository: PulsEventTotalRepository) {

    @Test
    fun saveAndRead() {
        val two = pulsEventTotalRepository.save(PulsEventTotal(oid=UUID.randomUUID().toString(), type = "Stilling visning", total = 2))
        assertNotNull(two.id)
        val inDB = pulsEventTotalRepository.findByOidAndType(two.oid, two.type)
        assertNotNull(inDB)
        val three = pulsEventTotalRepository.save(PulsEventTotal(oid=UUID.randomUUID().toString(), type = "Stilling visning", total = 3))
        val six = pulsEventTotalRepository.save(PulsEventTotal(oid=UUID.randomUUID().toString(), type = "Stilling visning", total = 6))
        val one = pulsEventTotalRepository.save(PulsEventTotal(oid=UUID.randomUUID().toString(), type = "Stilling visning", total = 1))
        val top4 = pulsEventTotalRepository.findByTypeOrderByTotalDesc("Stilling visning", Pageable.from(0, 4))
        assertEquals(4, top4.size)
    }
}

