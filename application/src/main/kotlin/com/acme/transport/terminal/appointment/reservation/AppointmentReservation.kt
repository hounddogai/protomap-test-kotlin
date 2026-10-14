package com.acme.transport.terminal.appointment.reservation

import com.acme.transport.kit.text.normalize

// The appointment reservation records of terminal's appointment work, and the rules, stores, and calculations
// around them.

/** Where a appointment reservation is in its life cycle. */
enum class AppointmentReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One appointment reservation, with what the planners need to act on it. */
data class AppointmentReservation(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var expiresAt: Int,
    var ownerId: String,
    var status: AppointmentReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentReservationStatus): Boolean {
        when (status) {
            AppointmentReservationStatus.IDLE -> {
                when (next) {
                    AppointmentReservationStatus.READY -> return true
                }
                return false
            }
            AppointmentReservationStatus.WARMING -> return next == AppointmentReservationStatus.DRAINING || next == AppointmentReservationStatus.STOPPED
            AppointmentReservationStatus.READY -> return next == AppointmentReservationStatus.DRAINING
            AppointmentReservationStatus.DRAINING -> return next == AppointmentReservationStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AppointmentReservationStatus) {
        if (!canMoveTo(next)) {
            error("appointment reservation $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): AppointmentReservation {
        return AppointmentReservation(
            code = code,
            title = title,
            pinned = pinned,
            expiresAt = expiresAt,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a appointment reservation in status idle. */
fun newAppointmentReservation(code: String, title: String): AppointmentReservation {
    return AppointmentReservation(
        code = code,
        title = title,
        pinned = false,
        expiresAt = 0,
        ownerId = "",
        status = AppointmentReservationStatus.IDLE,
    )
}
