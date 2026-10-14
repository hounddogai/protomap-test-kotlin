package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling appointment exception is in its life cycle. */
enum class RollingAppointmentExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One rolling appointment exception, with what the planners need to act on it. */
data class RollingAppointmentException(
    var code: String,
    var label: String,
    var pallets: Int,
    var cost: Double,
    var trailerId: String,
    var status: RollingAppointmentExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAppointmentExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAppointmentExceptionStatus): Boolean {
        when (status) {
            RollingAppointmentExceptionStatus.PLANNED -> {
                when (next) {
                    RollingAppointmentExceptionStatus.IN_PROGRESS, RollingAppointmentExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            RollingAppointmentExceptionStatus.SCHEDULED -> {
                when (next) {
                    RollingAppointmentExceptionStatus.COMPLETED, RollingAppointmentExceptionStatus.CANCELLED -> return true
                }
                return false
            }
            RollingAppointmentExceptionStatus.IN_PROGRESS -> return next == RollingAppointmentExceptionStatus.COMPLETED
            RollingAppointmentExceptionStatus.COMPLETED -> {
                when (next) {
                    RollingAppointmentExceptionStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingAppointmentExceptionStatus) {
        if (!canMoveTo(next)) {
            error("rolling appointment exception $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netVolume(): Double = round((pallets.toDouble() - cost) * 100) / 100

    fun snapshot(): RollingAppointmentException {
        return RollingAppointmentException(
            code = code,
            label = label,
            pallets = pallets,
            cost = cost,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling appointment exception in status planned. */
fun newRollingAppointmentException(code: String, label: String): RollingAppointmentException {
    return RollingAppointmentException(
        code = code,
        label = label,
        pallets = 0,
        cost = 0.0,
        trailerId = "",
        status = RollingAppointmentExceptionStatus.PLANNED,
    )
}
