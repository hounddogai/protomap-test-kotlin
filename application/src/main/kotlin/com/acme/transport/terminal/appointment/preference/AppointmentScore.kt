package com.acme.transport.terminal.appointment.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment score is in its life cycle. */
enum class AppointmentScoreStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A appointment score as the appointment team keeps it. */
data class AppointmentScore(
    var code: String,
    var name: String,
    var score: Double,
    var distanceKm: Double,
    var carrier: String,
    var cost: Double,
    var status: AppointmentScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentScoreStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentScoreStatus): Boolean {
        when (status) {
            AppointmentScoreStatus.IDLE -> return next == AppointmentScoreStatus.DRAINING
            AppointmentScoreStatus.WARMING -> return next == AppointmentScoreStatus.READY || next == AppointmentScoreStatus.STOPPED
            AppointmentScoreStatus.READY -> {
                when (next) {
                    AppointmentScoreStatus.DRAINING -> return true
                }
                return false
            }
            AppointmentScoreStatus.DRAINING -> return next == AppointmentScoreStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AppointmentScoreStatus) {
        if (!canMoveTo(next)) {
            error("appointment score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedTotal(): Double = round((score + distanceKm) * 100) / 100

    fun snapshot(): AppointmentScore {
        return AppointmentScore(
            code = code,
            name = name,
            score = score,
            distanceKm = distanceKm,
            carrier = carrier,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a appointment score in status idle. */
fun newAppointmentScore(code: String, name: String): AppointmentScore {
    return AppointmentScore(
        code = code,
        name = name,
        score = 0.0,
        distanceKm = 0.0,
        carrier = "",
        cost = 0.0,
        status = AppointmentScoreStatus.IDLE,
    )
}
