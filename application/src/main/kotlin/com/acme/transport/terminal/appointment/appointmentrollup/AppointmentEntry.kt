package com.acme.transport.terminal.appointment.appointmentrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment entry is in its life cycle. */
enum class AppointmentEntryStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One appointment entry, with what the planners need to act on it. */
data class AppointmentEntry(
    var code: String,
    var title: String,
    var visible: Boolean,
    var timezone: String,
    var batchSize: Int,
    var graceDays: Int,
    var status: AppointmentEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentEntryStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentEntryStatus): Boolean {
        when (status) {
            AppointmentEntryStatus.IDLE -> {
                when (next) {
                    AppointmentEntryStatus.READY, AppointmentEntryStatus.STOPPED -> return true
                }
                return false
            }
            AppointmentEntryStatus.WARMING -> {
                when (next) {
                    AppointmentEntryStatus.READY, AppointmentEntryStatus.DRAINING -> return true
                }
                return false
            }
            AppointmentEntryStatus.READY -> return next == AppointmentEntryStatus.DRAINING || next == AppointmentEntryStatus.STOPPED
            AppointmentEntryStatus.DRAINING -> return next == AppointmentEntryStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AppointmentEntryStatus) {
        if (!canMoveTo(next)) {
            error("appointment entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"appointment entry"} $code"

    fun weightedVolume(): Double = round(batchSize.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): AppointmentEntry {
        return AppointmentEntry(
            code = code,
            title = title,
            visible = visible,
            timezone = timezone,
            batchSize = batchSize,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a appointment entry in status idle. */
fun newAppointmentEntry(code: String, title: String): AppointmentEntry {
    return AppointmentEntry(
        code = code,
        title = title,
        visible = false,
        timezone = "",
        batchSize = 0,
        graceDays = 0,
        status = AppointmentEntryStatus.IDLE,
    )
}
