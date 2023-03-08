package no.nav.arbeidsplassen.puls.event

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import java.time.Instant

@MappedEntity
data class PulsEventTotal(
    @field:Id
    @field:GeneratedValue
    var id: Long? = null,
    val oid: String,
    val total: Long = 1,
    val type: String,
    @field:TypeDef(type = DataType.JSON)
    val properties: Map<String, Any> = emptyMap(),
    val created: Instant = Instant.now(),
    val updated: Instant = Instant.now()
)

fun PulsEventTotal.isNew(): Boolean = id == null


