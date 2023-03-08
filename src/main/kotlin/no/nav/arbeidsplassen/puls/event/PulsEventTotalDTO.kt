package no.nav.arbeidsplassen.puls.event

import io.micronaut.core.annotation.Introspected
import java.time.Instant

@Introspected
data class PulsEventTotalDTO(
    var id: Long? = null,
    val oid: String,
    val total: Long = 1,
    val type: String,
    val properties: Map<String, Any> = emptyMap(),
    val created: Instant = Instant.now(),
    val updated: Instant = Instant.now()
)

