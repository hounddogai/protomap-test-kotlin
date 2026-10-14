package com.acme.transport.customs.inspection.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive inspection forecast is in its life cycle. */
enum class AdaptiveInspectionForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One adaptive inspection forecast, with what the planners need to act on it. */
data class AdaptiveInspectionForecast(
    var code: String,
    var title: String,
    var label: String,
    var version: Int,
    var trailerId: String,
    var carrier: String,
    var maximum: Double,
    var region: String,
    var country: String,
    var depot: String,
    var status: AdaptiveInspectionForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveInspectionForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveInspectionForecastStatus): Boolean {
        when (status) {
            AdaptiveInspectionForecastStatus.PLANNED -> return next == AdaptiveInspectionForecastStatus.CANCELLED
            AdaptiveInspectionForecastStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveInspectionForecastStatus.COMPLETED, AdaptiveInspectionForecastStatus.CANCELLED -> return true
                }
                return false
            }
            AdaptiveInspectionForecastStatus.IN_PROGRESS -> return next == AdaptiveInspectionForecastStatus.COMPLETED || next == AdaptiveInspectionForecastStatus.CANCELLED
            AdaptiveInspectionForecastStatus.COMPLETED -> {
                when (next) {
                    AdaptiveInspectionForecastStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveInspectionForecastStatus) {
        if (!canMoveTo(next)) {
            error("adaptive inspection forecast $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$title]"

    fun effectiveTotal(): Double = round((version.toDouble() + maximum) * 100) / 100

    fun snapshot(): AdaptiveInspectionForecast {
        return AdaptiveInspectionForecast(
            code = code,
            title = title,
            label = label,
            version = version,
            trailerId = trailerId,
            carrier = carrier,
            maximum = maximum,
            region = region,
            country = country,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a adaptive inspection forecast in status planned. */
fun newAdaptiveInspectionForecast(code: String, title: String): AdaptiveInspectionForecast {
    return AdaptiveInspectionForecast(
        code = code,
        title = title,
        label = "",
        version = 0,
        trailerId = "",
        carrier = "",
        maximum = 0.0,
        region = "",
        country = "",
        depot = "",
        status = AdaptiveInspectionForecastStatus.PLANNED,
    )
}
