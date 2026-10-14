package com.acme.transport.terminal.appointment.appointmentsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive appointment forecast is in its life cycle. */
enum class AdaptiveAppointmentForecastStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One adaptive appointment forecast, with what the planners need to act on it. */
data class AdaptiveAppointmentForecast(
    var code: String,
    var name: String,
    var carrier: String,
    var durationMinutes: Int,
    var ownerId: String,
    var cost: Double,
    var tags: MutableList<String>,
    var status: AdaptiveAppointmentForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAppointmentForecastStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAppointmentForecastStatus): Boolean {
        when (status) {
            AdaptiveAppointmentForecastStatus.DRAFT -> return next == AdaptiveAppointmentForecastStatus.ACTIVE || next == AdaptiveAppointmentForecastStatus.CLOSED
            AdaptiveAppointmentForecastStatus.PENDING -> {
                when (next) {
                    AdaptiveAppointmentForecastStatus.ACTIVE, AdaptiveAppointmentForecastStatus.SUSPENDED -> return true
                }
                return false
            }
            AdaptiveAppointmentForecastStatus.ACTIVE -> return next == AdaptiveAppointmentForecastStatus.CLOSED
            AdaptiveAppointmentForecastStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveAppointmentForecastStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveAppointmentForecastStatus) {
        if (!canMoveTo(next)) {
            error("adaptive appointment forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive appointment forecast"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netLoad(): Double = round(cost * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): AdaptiveAppointmentForecast {
        return AdaptiveAppointmentForecast(
            code = code,
            name = name,
            carrier = carrier,
            durationMinutes = durationMinutes,
            ownerId = ownerId,
            cost = cost,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive appointment forecast in status draft. */
fun newAdaptiveAppointmentForecast(code: String, name: String): AdaptiveAppointmentForecast {
    return AdaptiveAppointmentForecast(
        code = code,
        name = name,
        carrier = "",
        durationMinutes = 0,
        ownerId = "",
        cost = 0.0,
        tags = mutableListOf<String>(),
        status = AdaptiveAppointmentForecastStatus.DRAFT,
    )
}
