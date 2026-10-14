package com.acme.transport.fleetops.leasereturn.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy lease return ledger is in its life cycle. */
enum class LegacyLeaseReturnLedgerStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A legacy lease return ledger as the lease return team keeps it. */
data class LegacyLeaseReturnLedger(
    var code: String,
    var name: String,
    var priority: Int,
    var pinned: Boolean,
    var depot: String,
    var createdAt: Int,
    var ownerId: String,
    var startsAt: Int,
    var status: LegacyLeaseReturnLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyLeaseReturnLedgerStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyLeaseReturnLedgerStatus): Boolean {
        when (status) {
            LegacyLeaseReturnLedgerStatus.REQUESTED -> {
                when (next) {
                    LegacyLeaseReturnLedgerStatus.EXPIRED -> return true
                }
                return false
            }
            LegacyLeaseReturnLedgerStatus.GRANTED -> return next == LegacyLeaseReturnLedgerStatus.EXPIRED
            LegacyLeaseReturnLedgerStatus.REVOKED -> {
                when (next) {
                    LegacyLeaseReturnLedgerStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyLeaseReturnLedgerStatus) {
        if (!canMoveTo(next)) {
            error("legacy lease return ledger $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"legacy lease return ledger"} $code"

    fun adjustedValue(): Double = round((createdAt.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): LegacyLeaseReturnLedger {
        return LegacyLeaseReturnLedger(
            code = code,
            name = name,
            priority = priority,
            pinned = pinned,
            depot = depot,
            createdAt = createdAt,
            ownerId = ownerId,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy lease return ledger in status requested. */
fun newLegacyLeaseReturnLedger(code: String, name: String): LegacyLeaseReturnLedger {
    return LegacyLeaseReturnLedger(
        code = code,
        name = name,
        priority = 0,
        pinned = false,
        depot = "",
        createdAt = 0,
        ownerId = "",
        startsAt = 0,
        status = LegacyLeaseReturnLedgerStatus.REQUESTED,
    )
}
