package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled appointment forecast is in its life cycle. */
enum class ScheduledAppointmentForecastStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled appointment forecast. Its code is unique within a tenant. */
data class ScheduledAppointmentForecast(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var contactEmail: String,
    var createdAt: Int,
    var trailerId: String,
    var segment: String,
    var fuelLiters: Double,
    var status: ScheduledAppointmentForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAppointmentForecastStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAppointmentForecastStatus): Boolean {
        when (status) {
            ScheduledAppointmentForecastStatus.NEW -> return next == ScheduledAppointmentForecastStatus.RUNNING
            ScheduledAppointmentForecastStatus.QUEUED -> {
                when (next) {
                    ScheduledAppointmentForecastStatus.RUNNING, ScheduledAppointmentForecastStatus.DONE -> return true
                }
                return false
            }
            ScheduledAppointmentForecastStatus.RUNNING -> return next == ScheduledAppointmentForecastStatus.DONE
            ScheduledAppointmentForecastStatus.DONE -> return next == ScheduledAppointmentForecastStatus.FAILED
        }
        return false
    }

    fun moveTo(next: ScheduledAppointmentForecastStatus) {
        if (!canMoveTo(next)) {
            error("scheduled appointment forecast $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled appointment forecast"} $code"

    fun effectiveVolume(): Double = round((createdAt.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): ScheduledAppointmentForecast {
        return ScheduledAppointmentForecast(
            code = code,
            title = title,
            distanceKm = distanceKm,
            contactEmail = contactEmail,
            createdAt = createdAt,
            trailerId = trailerId,
            segment = segment,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scheduled appointment forecast in status new. */
fun newScheduledAppointmentForecast(code: String, title: String): ScheduledAppointmentForecast {
    return ScheduledAppointmentForecast(
        code = code,
        title = title,
        distanceKm = 0.0,
        contactEmail = "",
        createdAt = 0,
        trailerId = "",
        segment = "",
        fuelLiters = 0.0,
        status = ScheduledAppointmentForecastStatus.NEW,
    )
}
