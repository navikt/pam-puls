package no.nav.arbeidsplassen.puls.event

import java.time.LocalDateTime
import java.util.*

data class PulsEventTotal(val id: UUID = UUID.randomUUID(), val oId: String?, val created: LocalDateTime = LocalDateTime.now(), val updated: LocalDateTime = LocalDateTime.now(),
                          val total: Long = 1, val type: String, val title: String?)


