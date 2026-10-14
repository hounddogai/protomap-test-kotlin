package com.acme.transport.fleetops.leasereturn.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lease return ledger records of fleetops's lease return work, and the rules, stores, and calculations
// around them.

/** Where a lease return ledger is in its life cycle. */
enum class LeaseReturnLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lease return ledger as the lease return team keeps it. */
data class LeaseReturnLedger(
    var code: String,
    var title: String,
    var cost: Double,
    var startsAt: Int,
    var lane: String,
    var checksum: String,
    var margin: Double,
    var status: LeaseReturnLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LeaseReturnLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LeaseReturnLedgerStatus): Boolean {
        when (status) {
            LeaseReturnLedgerStatus.PLANNED -> {
                when (next) {
                    LeaseReturnLedgerStatus.SCHEDULED, LeaseReturnLedgerStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LeaseReturnLedgerStatus.SCHEDULED -> {
                when (next) {
                    LeaseReturnLedgerStatus.CANCELLED -> return true
                }
                return false
            }
            LeaseReturnLedgerStatus.IN_PROGRESS -> return next == LeaseReturnLedgerStatus.CANCELLED
            LeaseReturnLedgerStatus.COMPLETED -> return next == LeaseReturnLedgerStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LeaseReturnLedgerStatus) {
        if (!canMoveTo(next)) {
            error("lease return ledger $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"lease return ledger"} $code"

    fun adjustedTotal(): Double = round((startsAt.toDouble() + margin) * 100) / 100

    fun snapshot(): LeaseReturnLedger {
        return LeaseReturnLedger(
            code = code,
            title = title,
            cost = cost,
            startsAt = startsAt,
            lane = lane,
            checksum = checksum,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a lease return ledger in status planned. */
fun newLeaseReturnLedger(code: String, title: String): LeaseReturnLedger {
    return LeaseReturnLedger(
        code = code,
        title = title,
        cost = 0.0,
        startsAt = 0,
        lane = "",
        checksum = "",
        margin = 0.0,
        status = LeaseReturnLedgerStatus.PLANNED,
    )
}
