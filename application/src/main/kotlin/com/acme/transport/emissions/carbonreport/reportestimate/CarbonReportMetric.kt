package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report metric is in its life cycle. */
enum class CarbonReportMetricStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One carbon report metric, with what the planners need to act on it. */
data class CarbonReportMetric(
    var code: String,
    var label: String,
    var startsAt: Int,
    var durationMinutes: Int,
    var source: String,
    var ceiling: Double,
    var stopCount: Int,
    var segment: String,
    var carrier: String,
    var fuelLiters: Double,
    var country: String,
    var status: CarbonReportMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportMetricStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportMetricStatus): Boolean {
        when (status) {
            CarbonReportMetricStatus.CREATED -> {
                when (next) {
                    CarbonReportMetricStatus.VALIDATED, CarbonReportMetricStatus.RETIRED -> return true
                }
                return false
            }
            CarbonReportMetricStatus.VALIDATED -> {
                when (next) {
                    CarbonReportMetricStatus.PUBLISHED -> return true
                }
                return false
            }
            CarbonReportMetricStatus.PUBLISHED -> {
                when (next) {
                    CarbonReportMetricStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CarbonReportMetricStatus) {
        if (!canMoveTo(next)) {
            error("carbon report metric $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedTotal(): Double = round((stopCount.toDouble() - ceiling) * 100) / 100

    fun snapshot(): CarbonReportMetric {
        return CarbonReportMetric(
            code = code,
            label = label,
            startsAt = startsAt,
            durationMinutes = durationMinutes,
            source = source,
            ceiling = ceiling,
            stopCount = stopCount,
            segment = segment,
            carrier = carrier,
            fuelLiters = fuelLiters,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a carbon report metric in status created. */
fun newCarbonReportMetric(code: String, label: String): CarbonReportMetric {
    return CarbonReportMetric(
        code = code,
        label = label,
        startsAt = 0,
        durationMinutes = 0,
        source = "",
        ceiling = 0.0,
        stopCount = 0,
        segment = "",
        carrier = "",
        fuelLiters = 0.0,
        country = "",
        status = CarbonReportMetricStatus.CREATED,
    )
}
