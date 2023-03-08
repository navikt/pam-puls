package no.nav.arbeidsplassen.puls.outbox

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import no.nav.arbeidsplassen.puls.event.PulsEventTotalDTO
import java.time.Instant

@MappedEntity
data class Outbox(
    @field:Id
    @field:GeneratedValue
    var id: Long? = null,
    val oid: String,
    @field:TypeDef(type = DataType.STRING)
    val type: OutboxType = OutboxType.PULSEVENTTOTAL,
    @field:TypeDef(type = DataType.STRING)
    val status: OutboxStatus = OutboxStatus.PENDING,
    @field:TypeDef(type = DataType.JSON)
    val payload: PulsEventTotalDTO,
    val updated: Instant = Instant.now()
)

fun Outbox.isNew(): Boolean = id == null

enum class OutboxType {
    PULSEVENTTOTAL
}

enum class OutboxStatus {
    PENDING, DONE, ERROR
}
