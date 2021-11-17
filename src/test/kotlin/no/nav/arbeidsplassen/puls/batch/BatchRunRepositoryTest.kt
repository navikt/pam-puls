package no.nav.arbeidsplassen.puls.batch

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit


@MicronautTest
class BatchRunRepositoryTest(private val batchRunRepository: BatchRunRepository) {

    @Test
    fun saveAndRead() {
        val from = Instant.now().minus(2, ChronoUnit.HOURS)
        val to = Instant.now()
        val batchRun = batchRunRepository.save(BatchRun(name = "amplitude-20211107-20211108", startTime = from, endTime = to ))
        println(batchRun.toString())
        assertNotNull(batchRun.id)
        val update = batchRun.copy(status=BatchRunStatus.DONE, totalEvents = 100)
        batchRunRepository.save(update)
        val inDb = batchRunRepository.findByName("amplitude-20211107-20211108")
        val notInDB = batchRunRepository.findByName("doesnotexist")
        assertNotNull(inDb)
        assertEquals(inDb?.status, BatchRunStatus.DONE)
        assertEquals(inDb?.totalEvents, 100)
        assertNull(notInDB)
        val exists = batchRunRepository.findByStartTimeGreaterThanEquals(from)
        assertNotNull(exists)
        assertTrue(exists.isNotEmpty())
        val newStart = to.plus(1, ChronoUnit.HOURS)
        assertNull(batchRunRepository.startTimeIntersectInterval(newStart))
        assertNotNull(batchRunRepository.startTimeIntersectInterval(from))
        assertNotNull(batchRunRepository.startTimeIntersectInterval(to))
    }
}
