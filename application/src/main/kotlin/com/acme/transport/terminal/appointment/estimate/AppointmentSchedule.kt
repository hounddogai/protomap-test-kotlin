package com.acme.transport.terminal.appointment.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment schedule is in its life cycle. */
enum class AppointmentScheduleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a appointment schedule and its current status. */
data class AppointmentSchedule(
    var code: String,
    var name: String,
    var score: Double,
    var label: String,
    var trailerId: String,
    var priority: Int,
    var createdAt: Int,
    var segment: String,
    var driverId: String,
    var attempts: Int,
    var status: AppointmentScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentScheduleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentScheduleStatus): Boolean {
        when (status) {
            AppointmentScheduleStatus.REQUESTED -> {
                when (next) {
                    AppointmentScheduleStatus.EXPIRED -> return true
                }
                return false
            }
            AppointmentScheduleStatus.GRANTED -> return next == AppointmentScheduleStatus.REVOKED
            AppointmentScheduleStatus.REVOKED -> return next == AppointmentScheduleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AppointmentScheduleStatus) {
        if (!canMoveTo(next)) {
            error("appointment schedule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun weightedTotal(): Double = round((createdAt.toDouble() - score) * 100) / 100

    fun snapshot(): AppointmentSchedule {
        return AppointmentSchedule(
            code = code,
            name = name,
            score = score,
            label = label,
            trailerId = trailerId,
            priority = priority,
            createdAt = createdAt,
            segment = segment,
            driverId = driverId,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a appointment schedule in status requested. */
fun newAppointmentSchedule(code: String, name: String): AppointmentSchedule {
    return AppointmentSchedule(
        code = code,
        name = name,
        score = 0.0,
        label = "",
        trailerId = "",
        priority = 0,
        createdAt = 0,
        segment = "",
        driverId = "",
        attempts = 0,
        status = AppointmentScheduleStatus.REQUESTED,
    )
}
