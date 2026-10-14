package com.acme.transport.emissions.carbonreport.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient carbon report variance records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a lenient carbon report variance is in its life cycle. */
enum class LenientCarbonReportVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a lenient carbon report variance and its current status. */
data class LenientCarbonReportVariance(
    var code: String,
    var label: String,
    var capacity: Int,
    var etaMinutes: Int,
    var distanceKm: Double,
    var segment: String,
    var status: LenientCarbonReportVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCarbonReportVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCarbonReportVarianceStatus): Boolean {
        when (status) {
            LenientCarbonReportVarianceStatus.IDLE -> return next == LenientCarbonReportVarianceStatus.WARMING || next == LenientCarbonReportVarianceStatus.STOPPED
            LenientCarbonReportVarianceStatus.WARMING -> return next == LenientCarbonReportVarianceStatus.DRAINING || next == LenientCarbonReportVarianceStatus.STOPPED
            LenientCarbonReportVarianceStatus.READY -> return next == LenientCarbonReportVarianceStatus.DRAINING
            LenientCarbonReportVarianceStatus.DRAINING -> {
                when (next) {
                    LenientCarbonReportVarianceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientCarbonReportVarianceStatus) {
        if (!canMoveTo(next)) {
            error("lenient carbon report variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedValue(): Double = round((capacity.toDouble() + distanceKm) * 100) / 100

    fun snapshot(): LenientCarbonReportVariance {
        return LenientCarbonReportVariance(
            code = code,
            label = label,
            capacity = capacity,
            etaMinutes = etaMinutes,
            distanceKm = distanceKm,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a lenient carbon report variance in status idle. */
fun newLenientCarbonReportVariance(code: String, label: String): LenientCarbonReportVariance {
    return LenientCarbonReportVariance(
        code = code,
        label = label,
        capacity = 0,
        etaMinutes = 0,
        distanceKm = 0.0,
        segment = "",
        status = LenientCarbonReportVarianceStatus.IDLE,
    )
}
