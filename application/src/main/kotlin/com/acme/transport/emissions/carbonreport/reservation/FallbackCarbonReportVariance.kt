package com.acme.transport.emissions.carbonreport.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback carbon report variance is in its life cycle. */
enum class FallbackCarbonReportVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a fallback carbon report variance and its current status. */
data class FallbackCarbonReportVariance(
    var code: String,
    var label: String,
    var name: String,
    var approved: Boolean,
    var startsAt: Int,
    var segment: String,
    var graceDays: Int,
    var retries: Int,
    var timezone: String,
    var unit: String,
    var ratio: Double,
    var status: FallbackCarbonReportVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackCarbonReportVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackCarbonReportVarianceStatus): Boolean {
        when (status) {
            FallbackCarbonReportVarianceStatus.CREATED -> return next == FallbackCarbonReportVarianceStatus.PUBLISHED || next == FallbackCarbonReportVarianceStatus.RETIRED
            FallbackCarbonReportVarianceStatus.VALIDATED -> {
                when (next) {
                    FallbackCarbonReportVarianceStatus.PUBLISHED, FallbackCarbonReportVarianceStatus.RETIRED -> return true
                }
                return false
            }
            FallbackCarbonReportVarianceStatus.PUBLISHED -> {
                when (next) {
                    FallbackCarbonReportVarianceStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackCarbonReportVarianceStatus) {
        if (!canMoveTo(next)) {
            error("fallback carbon report variance $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveExposure(): Double = round((graceDays.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackCarbonReportVariance {
        return FallbackCarbonReportVariance(
            code = code,
            label = label,
            name = name,
            approved = approved,
            startsAt = startsAt,
            segment = segment,
            graceDays = graceDays,
            retries = retries,
            timezone = timezone,
            unit = unit,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a fallback carbon report variance in status created. */
fun newFallbackCarbonReportVariance(code: String, label: String): FallbackCarbonReportVariance {
    return FallbackCarbonReportVariance(
        code = code,
        label = label,
        name = "",
        approved = false,
        startsAt = 0,
        segment = "",
        graceDays = 0,
        retries = 0,
        timezone = "",
        unit = "",
        ratio = 0.0,
        status = FallbackCarbonReportVarianceStatus.CREATED,
    )
}
