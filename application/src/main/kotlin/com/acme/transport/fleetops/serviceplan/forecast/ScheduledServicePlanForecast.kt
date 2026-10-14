package com.acme.transport.fleetops.serviceplan.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled service plan forecast records of fleetops's service plan work, and the rules, stores, and
// calculations around them.

/** Where a scheduled service plan forecast is in its life cycle. */
enum class ScheduledServicePlanForecastStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One scheduled service plan forecast, with what the planners need to act on it. */
data class ScheduledServicePlanForecast(
    var code: String,
    var label: String,
    var country: String,
    var attempts: Int,
    var batchSize: Int,
    var endsAt: Int,
    var archived: Boolean,
    var graceDays: Int,
    var discount: Double,
    var rate: Double,
    var status: ScheduledServicePlanForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledServicePlanForecastStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledServicePlanForecastStatus): Boolean {
        when (status) {
            ScheduledServicePlanForecastStatus.IDLE -> return next == ScheduledServicePlanForecastStatus.WARMING || next == ScheduledServicePlanForecastStatus.DRAINING
            ScheduledServicePlanForecastStatus.WARMING -> {
                when (next) {
                    ScheduledServicePlanForecastStatus.READY, ScheduledServicePlanForecastStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledServicePlanForecastStatus.READY -> {
                when (next) {
                    ScheduledServicePlanForecastStatus.DRAINING, ScheduledServicePlanForecastStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledServicePlanForecastStatus.DRAINING -> {
                when (next) {
                    ScheduledServicePlanForecastStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledServicePlanForecastStatus) {
        if (!canMoveTo(next)) {
            error("scheduled service plan forecast $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedTotal(): Double = round(batchSize.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): ScheduledServicePlanForecast {
        return ScheduledServicePlanForecast(
            code = code,
            label = label,
            country = country,
            attempts = attempts,
            batchSize = batchSize,
            endsAt = endsAt,
            archived = archived,
            graceDays = graceDays,
            discount = discount,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scheduled service plan forecast in status idle. */
fun newScheduledServicePlanForecast(code: String, label: String): ScheduledServicePlanForecast {
    return ScheduledServicePlanForecast(
        code = code,
        label = label,
        country = "",
        attempts = 0,
        batchSize = 0,
        endsAt = 0,
        archived = false,
        graceDays = 0,
        discount = 0.0,
        rate = 0.0,
        status = ScheduledServicePlanForecastStatus.IDLE,
    )
}
