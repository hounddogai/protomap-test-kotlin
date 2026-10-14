package com.acme.transport.terminal.appointment.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The pending appointment baseline records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a pending appointment baseline is in its life cycle. */
enum class PendingAppointmentBaselineStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One pending appointment baseline, with what the planners need to act on it. */
data class PendingAppointmentBaseline(
    var code: String,
    var name: String,
    var phone: String,
    var reviewer: String,
    var floorValue: Double,
    var odometer: Int,
    var status: PendingAppointmentBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAppointmentBaselineStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAppointmentBaselineStatus): Boolean {
        when (status) {
            PendingAppointmentBaselineStatus.OPEN -> {
                when (next) {
                    PendingAppointmentBaselineStatus.REOPENED, PendingAppointmentBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingAppointmentBaselineStatus.IN_REVIEW -> return next == PendingAppointmentBaselineStatus.RESOLVED || next == PendingAppointmentBaselineStatus.REOPENED
            PendingAppointmentBaselineStatus.RESOLVED -> {
                when (next) {
                    PendingAppointmentBaselineStatus.REOPENED, PendingAppointmentBaselineStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingAppointmentBaselineStatus.REOPENED -> return next == PendingAppointmentBaselineStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PendingAppointmentBaselineStatus) {
        if (!canMoveTo(next)) {
            error("pending appointment baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveLoad(): Double = round((odometer.toDouble() - floorValue) * 100) / 100

    fun snapshot(): PendingAppointmentBaseline {
        return PendingAppointmentBaseline(
            code = code,
            name = name,
            phone = phone,
            reviewer = reviewer,
            floorValue = floorValue,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending appointment baseline in status open. */
fun newPendingAppointmentBaseline(code: String, name: String): PendingAppointmentBaseline {
    return PendingAppointmentBaseline(
        code = code,
        name = name,
        phone = "",
        reviewer = "",
        floorValue = 0.0,
        odometer = 0,
        status = PendingAppointmentBaselineStatus.OPEN,
    )
}
