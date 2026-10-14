package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report variance is in its life cycle. */
enum class CarbonReportVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a carbon report variance and its current status. */
data class CarbonReportVariance(
    var code: String,
    var label: String,
    var ratio: Double,
    var durationMinutes: Int,
    var summary: String,
    var status: CarbonReportVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportVarianceStatus): Boolean {
        when (status) {
            CarbonReportVarianceStatus.CREATED -> {
                when (next) {
                    CarbonReportVarianceStatus.VALIDATED, CarbonReportVarianceStatus.RETIRED -> return true
                }
                return false
            }
            CarbonReportVarianceStatus.VALIDATED -> {
                when (next) {
                    CarbonReportVarianceStatus.PUBLISHED -> return true
                }
                return false
            }
            CarbonReportVarianceStatus.PUBLISHED -> return next == CarbonReportVarianceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CarbonReportVarianceStatus) {
        if (!canMoveTo(next)) {
            error("carbon report variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"carbon report variance"} $code"

    fun netExposure(): Double = round(durationMinutes.toDouble() * ratio * 100) / 100

    fun snapshot(): CarbonReportVariance {
        return CarbonReportVariance(
            code = code,
            label = label,
            ratio = ratio,
            durationMinutes = durationMinutes,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a carbon report variance in status created. */
fun newCarbonReportVariance(code: String, label: String): CarbonReportVariance {
    return CarbonReportVariance(
        code = code,
        label = label,
        ratio = 0.0,
        durationMinutes = 0,
        summary = "",
        status = CarbonReportVarianceStatus.CREATED,
    )
}
