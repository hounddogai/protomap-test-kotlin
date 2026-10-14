package com.acme.transport.emissions.carbonreport.reportagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient carbon report manifest is in its life cycle. */
enum class LenientCarbonReportManifestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a lenient carbon report manifest and its current status. */
data class LenientCarbonReportManifest(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var dock: String,
    var loadKg: Double,
    var margin: Double,
    var revision: Int,
    var summary: String,
    var status: LenientCarbonReportManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCarbonReportManifestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCarbonReportManifestStatus): Boolean {
        when (status) {
            LenientCarbonReportManifestStatus.DRAFT -> return next == LenientCarbonReportManifestStatus.SUSPENDED
            LenientCarbonReportManifestStatus.PENDING -> return next == LenientCarbonReportManifestStatus.CLOSED
            LenientCarbonReportManifestStatus.ACTIVE -> {
                when (next) {
                    LenientCarbonReportManifestStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientCarbonReportManifestStatus.SUSPENDED -> return next == LenientCarbonReportManifestStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientCarbonReportManifestStatus) {
        if (!canMoveTo(next)) {
            error("lenient carbon report manifest $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"lenient carbon report manifest"} $code"

    fun effectiveTotal(): Double = round(durationMinutes.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): LenientCarbonReportManifest {
        return LenientCarbonReportManifest(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            dock = dock,
            loadKg = loadKg,
            margin = margin,
            revision = revision,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient carbon report manifest in status draft. */
fun newLenientCarbonReportManifest(code: String, label: String): LenientCarbonReportManifest {
    return LenientCarbonReportManifest(
        code = code,
        label = label,
        durationMinutes = 0,
        dock = "",
        loadKg = 0.0,
        margin = 0.0,
        revision = 0,
        summary = "",
        status = LenientCarbonReportManifestStatus.DRAFT,
    )
}
