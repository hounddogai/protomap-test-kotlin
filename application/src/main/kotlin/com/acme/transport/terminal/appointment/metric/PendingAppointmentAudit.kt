package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending appointment audit is in its life cycle. */
enum class PendingAppointmentAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a pending appointment audit and its current status. */
data class PendingAppointmentAudit(
    var code: String,
    var name: String,
    var attempts: Int,
    var amount: Double,
    var capacity: Int,
    var status: PendingAppointmentAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAppointmentAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAppointmentAuditStatus): Boolean {
        when (status) {
            PendingAppointmentAuditStatus.OPEN -> {
                when (next) {
                    PendingAppointmentAuditStatus.RESOLVED, PendingAppointmentAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingAppointmentAuditStatus.IN_REVIEW -> {
                when (next) {
                    PendingAppointmentAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingAppointmentAuditStatus.RESOLVED -> return next == PendingAppointmentAuditStatus.ARCHIVED
            PendingAppointmentAuditStatus.REOPENED -> return next == PendingAppointmentAuditStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PendingAppointmentAuditStatus) {
        if (!canMoveTo(next)) {
            error("pending appointment audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun projectedTotal(): Double = round((amount + capacity.toDouble()) * 100) / 100

    fun snapshot(): PendingAppointmentAudit {
        return PendingAppointmentAudit(
            code = code,
            name = name,
            attempts = attempts,
            amount = amount,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending appointment audit in status open. */
fun newPendingAppointmentAudit(code: String, name: String): PendingAppointmentAudit {
    return PendingAppointmentAudit(
        code = code,
        name = name,
        attempts = 0,
        amount = 0.0,
        capacity = 0,
        status = PendingAppointmentAuditStatus.OPEN,
    )
}
