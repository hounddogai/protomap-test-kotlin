package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending appointment score is in its life cycle. */
enum class PendingAppointmentScoreStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending appointment score as the appointment team keeps it. */
data class PendingAppointmentScore(
    var code: String,
    var name: String,
    var unit: String,
    var active: Boolean,
    var quantity: Int,
    var score: Double,
    var graceDays: Int,
    var discount: Double,
    var attempts: Int,
    var status: PendingAppointmentScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAppointmentScoreStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAppointmentScoreStatus): Boolean {
        when (status) {
            PendingAppointmentScoreStatus.OPEN -> {
                when (next) {
                    PendingAppointmentScoreStatus.REOPENED, PendingAppointmentScoreStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingAppointmentScoreStatus.IN_REVIEW -> {
                when (next) {
                    PendingAppointmentScoreStatus.RESOLVED -> return true
                }
                return false
            }
            PendingAppointmentScoreStatus.RESOLVED -> return next == PendingAppointmentScoreStatus.REOPENED || next == PendingAppointmentScoreStatus.ARCHIVED
            PendingAppointmentScoreStatus.REOPENED -> {
                when (next) {
                    PendingAppointmentScoreStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingAppointmentScoreStatus) {
        if (!canMoveTo(next)) {
            error("pending appointment score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending appointment score"} $code"

    fun weightedLoad(): Double = round((attempts.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): PendingAppointmentScore {
        return PendingAppointmentScore(
            code = code,
            name = name,
            unit = unit,
            active = active,
            quantity = quantity,
            score = score,
            graceDays = graceDays,
            discount = discount,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a pending appointment score in status open. */
fun newPendingAppointmentScore(code: String, name: String): PendingAppointmentScore {
    return PendingAppointmentScore(
        code = code,
        name = name,
        unit = "",
        active = false,
        quantity = 0,
        score = 0.0,
        graceDays = 0,
        discount = 0.0,
        attempts = 0,
        status = PendingAppointmentScoreStatus.OPEN,
    )
}
