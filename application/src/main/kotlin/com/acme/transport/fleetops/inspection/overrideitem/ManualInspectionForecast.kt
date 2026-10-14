package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual inspection forecast is in its life cycle. */
enum class ManualInspectionForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One manual inspection forecast, with what the planners need to act on it. */
data class ManualInspectionForecast(
    var code: String,
    var title: String,
    var updatedAt: Int,
    var threshold: Double,
    var description: String,
    var discount: Double,
    var lane: String,
    var pallets: Int,
    var trailerId: String,
    var reviewer: String,
    var distanceKm: Double,
    var status: ManualInspectionForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualInspectionForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualInspectionForecastStatus): Boolean {
        when (status) {
            ManualInspectionForecastStatus.PLANNED -> return next == ManualInspectionForecastStatus.IN_PROGRESS || next == ManualInspectionForecastStatus.CANCELLED
            ManualInspectionForecastStatus.SCHEDULED -> return next == ManualInspectionForecastStatus.COMPLETED
            ManualInspectionForecastStatus.IN_PROGRESS -> {
                when (next) {
                    ManualInspectionForecastStatus.COMPLETED, ManualInspectionForecastStatus.CANCELLED -> return true
                }
                return false
            }
            ManualInspectionForecastStatus.COMPLETED -> {
                when (next) {
                    ManualInspectionForecastStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualInspectionForecastStatus) {
        if (!canMoveTo(next)) {
            error("manual inspection forecast $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun weightedValue(): Double = round(discount * threshold * 100) / 100

    fun snapshot(): ManualInspectionForecast {
        return ManualInspectionForecast(
            code = code,
            title = title,
            updatedAt = updatedAt,
            threshold = threshold,
            description = description,
            discount = discount,
            lane = lane,
            pallets = pallets,
            trailerId = trailerId,
            reviewer = reviewer,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a manual inspection forecast in status planned. */
fun newManualInspectionForecast(code: String, title: String): ManualInspectionForecast {
    return ManualInspectionForecast(
        code = code,
        title = title,
        updatedAt = 0,
        threshold = 0.0,
        description = "",
        discount = 0.0,
        lane = "",
        pallets = 0,
        trailerId = "",
        reviewer = "",
        distanceKm = 0.0,
        status = ManualInspectionForecastStatus.PLANNED,
    )
}
