package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.kit.text.normalize

/** Where a scheduled lease return ledger is in its life cycle. */
enum class ScheduledLeaseReturnLedgerStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled lease return ledger as the lease return team keeps it. */
data class ScheduledLeaseReturnLedger(
    var code: String,
    var label: String,
    var externalRef: String,
    var phone: String,
    var tags: MutableList<String>,
    var unit: String,
    var title: String,
    var amount: Double,
    var status: ScheduledLeaseReturnLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLeaseReturnLedgerStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLeaseReturnLedgerStatus): Boolean {
        when (status) {
            ScheduledLeaseReturnLedgerStatus.OPEN -> {
                when (next) {
                    ScheduledLeaseReturnLedgerStatus.REOPENED, ScheduledLeaseReturnLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledLeaseReturnLedgerStatus.IN_REVIEW -> return next == ScheduledLeaseReturnLedgerStatus.REOPENED
            ScheduledLeaseReturnLedgerStatus.RESOLVED -> {
                when (next) {
                    ScheduledLeaseReturnLedgerStatus.REOPENED, ScheduledLeaseReturnLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledLeaseReturnLedgerStatus.REOPENED -> {
                when (next) {
                    ScheduledLeaseReturnLedgerStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledLeaseReturnLedgerStatus) {
        if (!canMoveTo(next)) {
            error("scheduled lease return ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled lease return ledger"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = amount

    fun snapshot(): ScheduledLeaseReturnLedger {
        return ScheduledLeaseReturnLedger(
            code = code,
            label = label,
            externalRef = externalRef,
            phone = phone,
            tags = tags,
            unit = unit,
            title = title,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled lease return ledger in status open. */
fun newScheduledLeaseReturnLedger(code: String, label: String): ScheduledLeaseReturnLedger {
    return ScheduledLeaseReturnLedger(
        code = code,
        label = label,
        externalRef = "",
        phone = "",
        tags = mutableListOf<String>(),
        unit = "",
        title = "",
        amount = 0.0,
        status = ScheduledLeaseReturnLedgerStatus.OPEN,
    )
}
