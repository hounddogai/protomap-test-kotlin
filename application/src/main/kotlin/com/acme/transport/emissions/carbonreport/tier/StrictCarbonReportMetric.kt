package com.acme.transport.emissions.carbonreport.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict carbon report metric is in its life cycle. */
enum class StrictCarbonReportMetricStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A strict carbon report metric. Its code is unique within a tenant. */
data class StrictCarbonReportMetric(
    var code: String,
    var title: String,
    var phone: String,
    var pallets: Int,
    var startsAt: Int,
    var windowDays: Int,
    var trailerId: String,
    var status: StrictCarbonReportMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCarbonReportMetricStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCarbonReportMetricStatus): Boolean {
        when (status) {
            StrictCarbonReportMetricStatus.REQUESTED -> {
                when (next) {
                    StrictCarbonReportMetricStatus.REVOKED -> return true
                }
                return false
            }
            StrictCarbonReportMetricStatus.GRANTED -> return next == StrictCarbonReportMetricStatus.REVOKED || next == StrictCarbonReportMetricStatus.EXPIRED
            StrictCarbonReportMetricStatus.REVOKED -> return next == StrictCarbonReportMetricStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: StrictCarbonReportMetricStatus) {
        if (!canMoveTo(next)) {
            error("strict carbon report metric $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"strict carbon report metric"} $code"

    fun netLoad(): Double = round(pallets.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): StrictCarbonReportMetric {
        return StrictCarbonReportMetric(
            code = code,
            title = title,
            phone = phone,
            pallets = pallets,
            startsAt = startsAt,
            windowDays = windowDays,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict carbon report metric in status requested. */
fun newStrictCarbonReportMetric(code: String, title: String): StrictCarbonReportMetric {
    return StrictCarbonReportMetric(
        code = code,
        title = title,
        phone = "",
        pallets = 0,
        startsAt = 0,
        windowDays = 0,
        trailerId = "",
        status = StrictCarbonReportMetricStatus.REQUESTED,
    )
}
