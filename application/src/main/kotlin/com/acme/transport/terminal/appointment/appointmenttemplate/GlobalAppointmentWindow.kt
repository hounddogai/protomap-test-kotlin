package com.acme.transport.terminal.appointment.appointmenttemplate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global appointment window is in its life cycle. */
enum class GlobalAppointmentWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A global appointment window. Its code is unique within a tenant. */
data class GlobalAppointmentWindow(
    var code: String,
    var title: String,
    var driverId: String,
    var limit: Int,
    var endsAt: Int,
    var archived: Boolean,
    var carrier: String,
    var active: Boolean,
    var dock: String,
    var priority: Int,
    var checksum: String,
    var status: GlobalAppointmentWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAppointmentWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAppointmentWindowStatus): Boolean {
        when (status) {
            GlobalAppointmentWindowStatus.DRAFT -> return next == GlobalAppointmentWindowStatus.ACTIVE
            GlobalAppointmentWindowStatus.PENDING -> return next == GlobalAppointmentWindowStatus.SUSPENDED
            GlobalAppointmentWindowStatus.ACTIVE -> return next == GlobalAppointmentWindowStatus.SUSPENDED || next == GlobalAppointmentWindowStatus.CLOSED
            GlobalAppointmentWindowStatus.SUSPENDED -> {
                when (next) {
                    GlobalAppointmentWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalAppointmentWindowStatus) {
        if (!canMoveTo(next)) {
            error("global appointment window $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netVolume(): Double = round((priority.toDouble() - limit.toDouble()) * 100) / 100

    fun snapshot(): GlobalAppointmentWindow {
        return GlobalAppointmentWindow(
            code = code,
            title = title,
            driverId = driverId,
            limit = limit,
            endsAt = endsAt,
            archived = archived,
            carrier = carrier,
            active = active,
            dock = dock,
            priority = priority,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global appointment window in status draft. */
fun newGlobalAppointmentWindow(code: String, title: String): GlobalAppointmentWindow {
    return GlobalAppointmentWindow(
        code = code,
        title = title,
        driverId = "",
        limit = 0,
        endsAt = 0,
        archived = false,
        carrier = "",
        active = false,
        dock = "",
        priority = 0,
        checksum = "",
        status = GlobalAppointmentWindowStatus.DRAFT,
    )
}
