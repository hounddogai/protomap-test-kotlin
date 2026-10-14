package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report manifest is in its life cycle. */
enum class CarbonReportManifestStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A carbon report manifest as the carbon report team keeps it. */
data class CarbonReportManifest(
    var code: String,
    var title: String,
    var cost: Double,
    var createdAt: Int,
    var threshold: Double,
    var locale: String,
    var margin: Double,
    var etaMinutes: Int,
    var status: CarbonReportManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportManifestStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportManifestStatus): Boolean {
        when (status) {
            CarbonReportManifestStatus.PROPOSED -> {
                when (next) {
                    CarbonReportManifestStatus.REJECTED, CarbonReportManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
            CarbonReportManifestStatus.APPROVED -> return next == CarbonReportManifestStatus.REJECTED || next == CarbonReportManifestStatus.WITHDRAWN
            CarbonReportManifestStatus.REJECTED -> {
                when (next) {
                    CarbonReportManifestStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CarbonReportManifestStatus) {
        if (!canMoveTo(next)) {
            error("carbon report manifest $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun netLoad(): Double = round((etaMinutes.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): CarbonReportManifest {
        return CarbonReportManifest(
            code = code,
            title = title,
            cost = cost,
            createdAt = createdAt,
            threshold = threshold,
            locale = locale,
            margin = margin,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a carbon report manifest in status proposed. */
fun newCarbonReportManifest(code: String, title: String): CarbonReportManifest {
    return CarbonReportManifest(
        code = code,
        title = title,
        cost = 0.0,
        createdAt = 0,
        threshold = 0.0,
        locale = "",
        margin = 0.0,
        etaMinutes = 0,
        status = CarbonReportManifestStatus.PROPOSED,
    )
}
