package no.nav.arbeidsplassen.puls.batch

import java.io.File
import java.time.Instant
import java.time.OffsetDateTime


data class AmplitudeExportInfo(val tmpFile: File, val batchRunName:String, val size: Long, val startTime: OffsetDateTime, val endTime: OffsetDateTime)
