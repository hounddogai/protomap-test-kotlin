package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment window is in its life cycle. */
enum class AppointmentWindowStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a appointment window and its current status. */
data class AppointmentWindow(
    var code: String,
    var title: String,
    var revision: Int,
    var rate: Double,
    var shiftCode: String,
    var notes: String,
    var durationMinutes: Int,
    var pallets: Int,
    var windowDays: Int,
    var status: AppointmentWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentWindowStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentWindowStatus): Boolean {
        when (status) {
            AppointmentWindowStatus.PROPOSED -> {
                when (next) {
                    AppointmentWindowStatus.REJECTED, AppointmentWindowStatus.WITHDRAWN -> return true
                }
                return false
            }
            AppointmentWindowStatus.APPROVED -> {
                when (next) {
                    AppointmentWindowStatus.REJECTED -> return true
                }
                return false
            }
            AppointmentWindowStatus.REJECTED -> return next == AppointmentWindowStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AppointmentWindowStatus) {
        if (!canMoveTo(next)) {
            error("appointment window $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"appointment window"} $code"

    fun projectedVolume(): Double = round((windowDays.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): AppointmentWindow {
        return AppointmentWindow(
            code = code,
            title = title,
            revision = revision,
            rate = rate,
            shiftCode = shiftCode,
            notes = notes,
            durationMinutes = durationMinutes,
            pallets = pallets,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a appointment window in status proposed. */
fun newAppointmentWindow(code: String, title: String): AppointmentWindow {
    return AppointmentWindow(
        code = code,
        title = title,
        revision = 0,
        rate = 0.0,
        shiftCode = "",
        notes = "",
        durationMinutes = 0,
        pallets = 0,
        windowDays = 0,
        status = AppointmentWindowStatus.PROPOSED,
    )
}
