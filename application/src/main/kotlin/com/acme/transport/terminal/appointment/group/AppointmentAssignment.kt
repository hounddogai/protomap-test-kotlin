package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.text.normalize

/** Where a appointment assignment is in its life cycle. */
enum class AppointmentAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One appointment assignment, with what the planners need to act on it. */
data class AppointmentAssignment(
    var code: String,
    var name: String,
    var phone: String,
    var notes: String,
    var capacity: Int,
    var description: String,
    var status: AppointmentAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentAssignmentStatus): Boolean {
        when (status) {
            AppointmentAssignmentStatus.PLANNED -> return next == AppointmentAssignmentStatus.CANCELLED
            AppointmentAssignmentStatus.SCHEDULED -> return next == AppointmentAssignmentStatus.CANCELLED
            AppointmentAssignmentStatus.IN_PROGRESS -> {
                when (next) {
                    AppointmentAssignmentStatus.COMPLETED, AppointmentAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            AppointmentAssignmentStatus.COMPLETED -> {
                when (next) {
                    AppointmentAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("appointment assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"appointment assignment"} $code"

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): AppointmentAssignment {
        return AppointmentAssignment(
            code = code,
            name = name,
            phone = phone,
            notes = notes,
            capacity = capacity,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a appointment assignment in status planned. */
fun newAppointmentAssignment(code: String, name: String): AppointmentAssignment {
    return AppointmentAssignment(
        code = code,
        name = name,
        phone = "",
        notes = "",
        capacity = 0,
        description = "",
        status = AppointmentAssignmentStatus.PLANNED,
    )
}
