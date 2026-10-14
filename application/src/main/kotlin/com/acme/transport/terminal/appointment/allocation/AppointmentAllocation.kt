package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The appointment allocation records of terminal's appointment work, and the rules, stores, and calculations
// around them.

/** Where a appointment allocation is in its life cycle. */
enum class AppointmentAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A appointment allocation as the appointment team keeps it. */
data class AppointmentAllocation(
    var code: String,
    var title: String,
    var revision: Int,
    var archived: Boolean,
    var limit: Int,
    var attempts: Int,
    var loadKg: Double,
    var status: AppointmentAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentAllocationStatus): Boolean {
        when (status) {
            AppointmentAllocationStatus.PLANNED -> return next == AppointmentAllocationStatus.COMPLETED || next == AppointmentAllocationStatus.CANCELLED
            AppointmentAllocationStatus.SCHEDULED -> {
                when (next) {
                    AppointmentAllocationStatus.IN_PROGRESS, AppointmentAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            AppointmentAllocationStatus.IN_PROGRESS -> {
                when (next) {
                    AppointmentAllocationStatus.COMPLETED -> return true
                }
                return false
            }
            AppointmentAllocationStatus.COMPLETED -> {
                when (next) {
                    AppointmentAllocationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentAllocationStatus) {
        if (!canMoveTo(next)) {
            error("appointment allocation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"appointment allocation"} $code"

    fun netExposure(): Double = round(attempts.toDouble() * loadKg * 100) / 100

    fun snapshot(): AppointmentAllocation {
        return AppointmentAllocation(
            code = code,
            title = title,
            revision = revision,
            archived = archived,
            limit = limit,
            attempts = attempts,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a appointment allocation in status planned. */
fun newAppointmentAllocation(code: String, title: String): AppointmentAllocation {
    return AppointmentAllocation(
        code = code,
        title = title,
        revision = 0,
        archived = false,
        limit = 0,
        attempts = 0,
        loadKg = 0.0,
        status = AppointmentAllocationStatus.PLANNED,
    )
}
