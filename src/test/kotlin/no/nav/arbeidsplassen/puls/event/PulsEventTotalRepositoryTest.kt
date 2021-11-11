package no.nav.arbeidsplassen.puls.event

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest
class PulsEventTotalRepositoryTest(private val pulsEventTotalRepository: PulsEventTotalRepository) {

    @Test
    fun saveAndRead() {
        val saved = pulsEventTotalRepository.save(PulsEventTotal(oid=UUID.randomUUID().toString(), type = "Stilling visning"))
        assertNotNull(saved.id)
        val inDB = pulsEventTotalRepository.findByOidAndType(saved.oid, saved.type)
        assertNotNull(inDB)
    }
}

