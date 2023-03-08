package no.nav.arbeidsplassen.puls.batch

import java.io.File
import java.time.Instant

data class AmplitudeExportInfo(val tmpFile: File, val batchRunName:String, val startTime: Instant, val endTime: Instant)
