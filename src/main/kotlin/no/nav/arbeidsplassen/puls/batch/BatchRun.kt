package no.nav.arbeidsplassen.puls.batch

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import java.time.Instant

@MappedEntity
data class BatchRun(
    @field:Id
    @field:GeneratedValue
    var id: Long? = null,
    val name: String,
    @field:TypeDef(type = DataType.STRING)
    val status: BatchRunStatus = BatchRunStatus.PENDING,
    val updated: Instant = Instant.now(),
    val totalEvents: Int = 0,
    val startTime: Instant = Instant.now(),
    val endTime: Instant = Instant.now()
)

fun BatchRun.isNew(): Boolean = id == null
