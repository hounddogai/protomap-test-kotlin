package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.text.normalize

/** Where a pending violation ledger is in its life cycle. */
enum class PendingViolationLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One pending violation ledger, with what the planners need to act on it. */
data class PendingViolationLedger(
    var code: String,
    var title: String,
    var vehicleId: String,
    var currency: String,
    var amount: Double,
    var status: PendingViolationLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingViolationLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingViolationLedgerStatus): Boolean {
        when (status) {
            PendingViolationLedgerStatus.PLANNED -> {
                when (next) {
                    PendingViolationLedgerStatus.SCHEDULED, PendingViolationLedgerStatus.COMPLETED -> return true
                }
                return false
            }
            PendingViolationLedgerStatus.SCHEDULED -> {
                when (next) {
                    PendingViolationLedgerStatus.COMPLETED, PendingViolationLedgerStatus.CANCELLED -> return true
                }
                return false
            }
            PendingViolationLedgerStatus.IN_PROGRESS -> {
                when (next) {
                    PendingViolationLedgerStatus.COMPLETED -> return true
                }
                return false
            }
            PendingViolationLedgerStatus.COMPLETED -> return next == PendingViolationLedgerStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingViolationLedgerStatus) {
        if (!canMoveTo(next)) {
            error("pending violation ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = amount

    fun snapshot(): PendingViolationLedger {
        return PendingViolationLedger(
            code = code,
            title = title,
            vehicleId = vehicleId,
            currency = currency,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending violation ledger in status planned. */
fun newPendingViolationLedger(code: String, title: String): PendingViolationLedger {
    return PendingViolationLedger(
        code = code,
        title = title,
        vehicleId = "",
        currency = "",
        amount = 0.0,
        status = PendingViolationLedgerStatus.PLANNED,
    )
}
