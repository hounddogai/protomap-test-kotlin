package com.acme.transport.terminal.appointment.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment forecast is in its life cycle. */
enum class AppointmentForecastStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a appointment forecast and its current status. */
data class AppointmentForecast(
    var code: String,
    var label: String,
    var approved: Boolean,
    var priority: Int,
    var minimum: Double,
    var sequence: Int,
    var status: AppointmentForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentForecastStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentForecastStatus): Boolean {
        when (status) {
            AppointmentForecastStatus.REQUESTED -> return next == AppointmentForecastStatus.GRANTED || next == AppointmentForecastStatus.REVOKED
            AppointmentForecastStatus.GRANTED -> {
                when (next) {
                    AppointmentForecastStatus.REVOKED, AppointmentForecastStatus.EXPIRED -> return true
                }
                return false
            }
            AppointmentForecastStatus.REVOKED -> return next == AppointmentForecastStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AppointmentForecastStatus) {
        if (!canMoveTo(next)) {
            error("appointment forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netLoad(): Double = round((sequence.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): AppointmentForecast {
        return AppointmentForecast(
            code = code,
            label = label,
            approved = approved,
            priority = priority,
            minimum = minimum,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a appointment forecast in status requested. */
fun newAppointmentForecast(code: String, label: String): AppointmentForecast {
    return AppointmentForecast(
        code = code,
        label = label,
        approved = false,
        priority = 0,
        minimum = 0.0,
        sequence = 0,
        status = AppointmentForecastStatus.REQUESTED,
    )
}
