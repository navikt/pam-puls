package no.nav.arbeidsplassen.puls.event

import io.micronaut.core.annotation.Introspected
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Introspected
data class PulsEventTotalDTO (
    var id: Long? = null,
    val oid: String,
    val total: Long = 1,
    val type: String,
    val properties : Map<String, Any> = emptyMap(),
    val created: OffsetDateTime = OffsetDateTime.now(),
    val updated: OffsetDateTime = OffsetDateTime.now()
)

