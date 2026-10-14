package com.acme.transport.fleetops.serviceplan.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy service plan forecast is in its life cycle. */
enum class LegacyServicePlanForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A legacy service plan forecast as the service plan team keeps it. */
data class LegacyServicePlanForecast(
    var code: String,
    var title: String,
    var active: Boolean,
    var floorValue: Double,
    var version: Int,
    var trailerId: String,
    var status: LegacyServicePlanForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyServicePlanForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyServicePlanForecastStatus): Boolean {
        when (status) {
            LegacyServicePlanForecastStatus.OPEN -> return next == LegacyServicePlanForecastStatus.REOPENED || next == LegacyServicePlanForecastStatus.ARCHIVED
            LegacyServicePlanForecastStatus.IN_REVIEW -> {
                when (next) {
                    LegacyServicePlanForecastStatus.RESOLVED, LegacyServicePlanForecastStatus.REOPENED -> return true
                }
                return false
            }
            LegacyServicePlanForecastStatus.RESOLVED -> {
                when (next) {
                    LegacyServicePlanForecastStatus.REOPENED -> return true
                }
                return false
            }
            LegacyServicePlanForecastStatus.REOPENED -> return next == LegacyServicePlanForecastStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LegacyServicePlanForecastStatus) {
        if (!canMoveTo(next)) {
            error("legacy service plan forecast $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"legacy service plan forecast"} $code"

    fun weightedValue(): Double = round((version.toDouble() - floorValue) * 100) / 100

    fun snapshot(): LegacyServicePlanForecast {
        return LegacyServicePlanForecast(
            code = code,
            title = title,
            active = active,
            floorValue = floorValue,
            version = version,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy service plan forecast in status open. */
fun newLegacyServicePlanForecast(code: String, title: String): LegacyServicePlanForecast {
    return LegacyServicePlanForecast(
        code = code,
        title = title,
        active = false,
        floorValue = 0.0,
        version = 0,
        trailerId = "",
        status = LegacyServicePlanForecastStatus.OPEN,
    )
}
