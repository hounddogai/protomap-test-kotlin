package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled utilization forecast is in its life cycle. */
enum class ScheduledUtilizationForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled utilization forecast. Its code is unique within a tenant. */
data class ScheduledUtilizationForecast(
    var code: String,
    var name: String,
    var trailerId: String,
    var cost: Double,
    var sequence: Int,
    var threshold: Double,
    var region: String,
    var status: ScheduledUtilizationForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledUtilizationForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledUtilizationForecastStatus): Boolean {
        when (status) {
            ScheduledUtilizationForecastStatus.OPEN -> {
                when (next) {
                    ScheduledUtilizationForecastStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledUtilizationForecastStatus.IN_REVIEW -> return next == ScheduledUtilizationForecastStatus.RESOLVED
            ScheduledUtilizationForecastStatus.RESOLVED -> return next == ScheduledUtilizationForecastStatus.ARCHIVED
            ScheduledUtilizationForecastStatus.REOPENED -> {
                when (next) {
                    ScheduledUtilizationForecastStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledUtilizationForecastStatus) {
        if (!canMoveTo(next)) {
            error("scheduled utilization forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedValue(): Double = round((threshold + cost) * 100) / 100

    fun snapshot(): ScheduledUtilizationForecast {
        return ScheduledUtilizationForecast(
            code = code,
            name = name,
            trailerId = trailerId,
            cost = cost,
            sequence = sequence,
            threshold = threshold,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled utilization forecast in status open. */
fun newScheduledUtilizationForecast(code: String, name: String): ScheduledUtilizationForecast {
    return ScheduledUtilizationForecast(
        code = code,
        name = name,
        trailerId = "",
        cost = 0.0,
        sequence = 0,
        threshold = 0.0,
        region = "",
        status = ScheduledUtilizationForecastStatus.OPEN,
    )
}
