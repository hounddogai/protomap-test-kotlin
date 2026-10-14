package com.acme.transport.terminal.appointment.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The manual appointment window records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a manual appointment window is in its life cycle. */
enum class ManualAppointmentWindowStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a manual appointment window and its current status. */
data class ManualAppointmentWindow(
    var code: String,
    var label: String,
    var shiftCode: String,
    var attempts: Int,
    var endsAt: Int,
    var archived: Boolean,
    var minimum: Double,
    var channel: String,
    var status: ManualAppointmentWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAppointmentWindowStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAppointmentWindowStatus): Boolean {
        when (status) {
            ManualAppointmentWindowStatus.PLANNED -> return next == ManualAppointmentWindowStatus.SCHEDULED
            ManualAppointmentWindowStatus.SCHEDULED -> return next == ManualAppointmentWindowStatus.COMPLETED || next == ManualAppointmentWindowStatus.CANCELLED
            ManualAppointmentWindowStatus.IN_PROGRESS -> {
                when (next) {
                    ManualAppointmentWindowStatus.COMPLETED -> return true
                }
                return false
            }
            ManualAppointmentWindowStatus.COMPLETED -> return next == ManualAppointmentWindowStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: ManualAppointmentWindowStatus) {
        if (!canMoveTo(next)) {
            error("manual appointment window $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"manual appointment window"} $code"

    fun weightedValue(): Double = round(attempts.toDouble() * minimum * 100) / 100

    fun snapshot(): ManualAppointmentWindow {
        return ManualAppointmentWindow(
            code = code,
            label = label,
            shiftCode = shiftCode,
            attempts = attempts,
            endsAt = endsAt,
            archived = archived,
            minimum = minimum,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a manual appointment window in status planned. */
fun newManualAppointmentWindow(code: String, label: String): ManualAppointmentWindow {
    return ManualAppointmentWindow(
        code = code,
        label = label,
        shiftCode = "",
        attempts = 0,
        endsAt = 0,
        archived = false,
        minimum = 0.0,
        channel = "",
        status = ManualAppointmentWindowStatus.PLANNED,
    )
}
