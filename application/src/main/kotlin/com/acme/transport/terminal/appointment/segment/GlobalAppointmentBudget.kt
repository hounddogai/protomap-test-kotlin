package com.acme.transport.terminal.appointment.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global appointment budget is in its life cycle. */
enum class GlobalAppointmentBudgetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One global appointment budget, with what the planners need to act on it. */
data class GlobalAppointmentBudget(
    var code: String,
    var label: String,
    var dock: String,
    var pallets: Int,
    var expiresAt: Int,
    var status: GlobalAppointmentBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAppointmentBudgetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAppointmentBudgetStatus): Boolean {
        when (status) {
            GlobalAppointmentBudgetStatus.PROPOSED -> {
                when (next) {
                    GlobalAppointmentBudgetStatus.WITHDRAWN -> return true
                }
                return false
            }
            GlobalAppointmentBudgetStatus.APPROVED -> return next == GlobalAppointmentBudgetStatus.REJECTED || next == GlobalAppointmentBudgetStatus.WITHDRAWN
            GlobalAppointmentBudgetStatus.REJECTED -> return next == GlobalAppointmentBudgetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalAppointmentBudgetStatus) {
        if (!canMoveTo(next)) {
            error("global appointment budget $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"global appointment budget"} $code"

    fun projectedLoad(): Double = round((expiresAt.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): GlobalAppointmentBudget {
        return GlobalAppointmentBudget(
            code = code,
            label = label,
            dock = dock,
            pallets = pallets,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global appointment budget in status proposed. */
fun newGlobalAppointmentBudget(code: String, label: String): GlobalAppointmentBudget {
    return GlobalAppointmentBudget(
        code = code,
        label = label,
        dock = "",
        pallets = 0,
        expiresAt = 0,
        status = GlobalAppointmentBudgetStatus.PROPOSED,
    )
}
