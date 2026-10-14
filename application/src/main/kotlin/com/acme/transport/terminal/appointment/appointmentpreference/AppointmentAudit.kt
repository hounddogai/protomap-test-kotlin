package com.acme.transport.terminal.appointment.appointmentpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment audit is in its life cycle. */
enum class AppointmentAuditStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One appointment audit, with what the planners need to act on it. */
data class AppointmentAudit(
    var code: String,
    var name: String,
    var batchSize: Int,
    var depot: String,
    var budget: Double,
    var archived: Boolean,
    var status: AppointmentAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentAuditStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentAuditStatus): Boolean {
        when (status) {
            AppointmentAuditStatus.PROPOSED -> {
                when (next) {
                    AppointmentAuditStatus.APPROVED, AppointmentAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
            AppointmentAuditStatus.APPROVED -> return next == AppointmentAuditStatus.WITHDRAWN
            AppointmentAuditStatus.REJECTED -> {
                when (next) {
                    AppointmentAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentAuditStatus) {
        if (!canMoveTo(next)) {
            error("appointment audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveValue(): Double = round((budget - batchSize.toDouble()) * 100) / 100

    fun snapshot(): AppointmentAudit {
        return AppointmentAudit(
            code = code,
            name = name,
            batchSize = batchSize,
            depot = depot,
            budget = budget,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a appointment audit in status proposed. */
fun newAppointmentAudit(code: String, name: String): AppointmentAudit {
    return AppointmentAudit(
        code = code,
        name = name,
        batchSize = 0,
        depot = "",
        budget = 0.0,
        archived = false,
        status = AppointmentAuditStatus.PROPOSED,
    )
}
