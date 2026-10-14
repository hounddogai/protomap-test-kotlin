package com.acme.transport.crewing.availability.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled availability forecast is in its life cycle. */
enum class ScheduledAvailabilityForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A scheduled availability forecast. Its code is unique within a tenant. */
data class ScheduledAvailabilityForecast(
    var code: String,
    var title: String,
    var threshold: Double,
    var endsAt: Int,
    var unit: String,
    var revision: Int,
    var timezone: String,
    var stopCount: Int,
    var graceDays: Int,
    var status: ScheduledAvailabilityForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAvailabilityForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAvailabilityForecastStatus): Boolean {
        when (status) {
            ScheduledAvailabilityForecastStatus.PLANNED -> return next == ScheduledAvailabilityForecastStatus.CANCELLED
            ScheduledAvailabilityForecastStatus.SCHEDULED -> {
                when (next) {
                    ScheduledAvailabilityForecastStatus.IN_PROGRESS -> return true
                }
                return false
            }
            ScheduledAvailabilityForecastStatus.IN_PROGRESS -> {
                when (next) {
                    ScheduledAvailabilityForecastStatus.COMPLETED, ScheduledAvailabilityForecastStatus.CANCELLED -> return true
                }
                return false
            }
            ScheduledAvailabilityForecastStatus.COMPLETED -> return next == ScheduledAvailabilityForecastStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ScheduledAvailabilityForecastStatus) {
        if (!canMoveTo(next)) {
            error("scheduled availability forecast $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun weightedExposure(): Double = round(endsAt.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): ScheduledAvailabilityForecast {
        return ScheduledAvailabilityForecast(
            code = code,
            title = title,
            threshold = threshold,
            endsAt = endsAt,
            unit = unit,
            revision = revision,
            timezone = timezone,
            stopCount = stopCount,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled availability forecast in status planned. */
fun newScheduledAvailabilityForecast(code: String, title: String): ScheduledAvailabilityForecast {
    return ScheduledAvailabilityForecast(
        code = code,
        title = title,
        threshold = 0.0,
        endsAt = 0,
        unit = "",
        revision = 0,
        timezone = "",
        stopCount = 0,
        graceDays = 0,
        status = ScheduledAvailabilityForecastStatus.PLANNED,
    )
}
