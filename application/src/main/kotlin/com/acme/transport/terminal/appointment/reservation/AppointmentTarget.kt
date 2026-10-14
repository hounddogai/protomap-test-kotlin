package com.acme.transport.terminal.appointment.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment target is in its life cycle. */
enum class AppointmentTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A appointment target. Its code is unique within a tenant. */
data class AppointmentTarget(
    var code: String,
    var name: String,
    var priority: Int,
    var phone: String,
    var ceiling: Double,
    var status: AppointmentTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentTargetStatus): Boolean {
        when (status) {
            AppointmentTargetStatus.OPEN -> return next == AppointmentTargetStatus.IN_REVIEW || next == AppointmentTargetStatus.REOPENED
            AppointmentTargetStatus.IN_REVIEW -> {
                when (next) {
                    AppointmentTargetStatus.RESOLVED -> return true
                }
                return false
            }
            AppointmentTargetStatus.RESOLVED -> {
                when (next) {
                    AppointmentTargetStatus.REOPENED, AppointmentTargetStatus.ARCHIVED -> return true
                }
                return false
            }
            AppointmentTargetStatus.REOPENED -> return next == AppointmentTargetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AppointmentTargetStatus) {
        if (!canMoveTo(next)) {
            error("appointment target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"appointment target"} $code"

    fun projectedTotal(): Double = round((priority.toDouble() + ceiling) * 100) / 100

    fun snapshot(): AppointmentTarget {
        return AppointmentTarget(
            code = code,
            name = name,
            priority = priority,
            phone = phone,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a appointment target in status open. */
fun newAppointmentTarget(code: String, name: String): AppointmentTarget {
    return AppointmentTarget(
        code = code,
        name = name,
        priority = 0,
        phone = "",
        ceiling = 0.0,
        status = AppointmentTargetStatus.OPEN,
    )
}
