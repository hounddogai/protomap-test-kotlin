package com.acme.transport.fleetops.leasereturn.entry

import com.acme.transport.kit.text.normalize

/** Where a primary lease return budget is in its life cycle. */
enum class PrimaryLeaseReturnBudgetStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a primary lease return budget and its current status. */
data class PrimaryLeaseReturnBudget(
    var code: String,
    var label: String,
    var currency: String,
    var title: String,
    var rate: Double,
    var segment: String,
    var approved: Boolean,
    var status: PrimaryLeaseReturnBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryLeaseReturnBudgetStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryLeaseReturnBudgetStatus): Boolean {
        when (status) {
            PrimaryLeaseReturnBudgetStatus.IDLE -> return next == PrimaryLeaseReturnBudgetStatus.WARMING
            PrimaryLeaseReturnBudgetStatus.WARMING -> return next == PrimaryLeaseReturnBudgetStatus.DRAINING
            PrimaryLeaseReturnBudgetStatus.READY -> return next == PrimaryLeaseReturnBudgetStatus.DRAINING || next == PrimaryLeaseReturnBudgetStatus.STOPPED
            PrimaryLeaseReturnBudgetStatus.DRAINING -> {
                when (next) {
                    PrimaryLeaseReturnBudgetStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryLeaseReturnBudgetStatus) {
        if (!canMoveTo(next)) {
            error("primary lease return budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"primary lease return budget"} $code"

    fun measuredValue(): Double = rate

    fun snapshot(): PrimaryLeaseReturnBudget {
        return PrimaryLeaseReturnBudget(
            code = code,
            label = label,
            currency = currency,
            title = title,
            rate = rate,
            segment = segment,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary lease return budget in status idle. */
fun newPrimaryLeaseReturnBudget(code: String, label: String): PrimaryLeaseReturnBudget {
    return PrimaryLeaseReturnBudget(
        code = code,
        label = label,
        currency = "",
        title = "",
        rate = 0.0,
        segment = "",
        approved = false,
        status = PrimaryLeaseReturnBudgetStatus.IDLE,
    )
}
