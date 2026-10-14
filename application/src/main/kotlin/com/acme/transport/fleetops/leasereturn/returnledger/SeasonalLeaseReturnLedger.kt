package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal lease return ledger records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a seasonal lease return ledger is in its life cycle. */
enum class SeasonalLeaseReturnLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A seasonal lease return ledger as the lease return team keeps it. */
data class SeasonalLeaseReturnLedger(
    var code: String,
    var title: String,
    var visible: Boolean,
    var region: String,
    var dock: String,
    var pinned: Boolean,
    var limit: Int,
    var score: Double,
    var status: SeasonalLeaseReturnLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalLeaseReturnLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalLeaseReturnLedgerStatus): Boolean {
        when (status) {
            SeasonalLeaseReturnLedgerStatus.PROPOSED -> return next == SeasonalLeaseReturnLedgerStatus.REJECTED
            SeasonalLeaseReturnLedgerStatus.APPROVED -> {
                when (next) {
                    SeasonalLeaseReturnLedgerStatus.REJECTED -> return true
                }
                return false
            }
            SeasonalLeaseReturnLedgerStatus.REJECTED -> return next == SeasonalLeaseReturnLedgerStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: SeasonalLeaseReturnLedgerStatus) {
        if (!canMoveTo(next)) {
            error("seasonal lease return ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedLoad(): Double = round((limit.toDouble() - score) * 100) / 100

    fun snapshot(): SeasonalLeaseReturnLedger {
        return SeasonalLeaseReturnLedger(
            code = code,
            title = title,
            visible = visible,
            region = region,
            dock = dock,
            pinned = pinned,
            limit = limit,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a seasonal lease return ledger in status proposed. */
fun newSeasonalLeaseReturnLedger(code: String, title: String): SeasonalLeaseReturnLedger {
    return SeasonalLeaseReturnLedger(
        code = code,
        title = title,
        visible = false,
        region = "",
        dock = "",
        pinned = false,
        limit = 0,
        score = 0.0,
        status = SeasonalLeaseReturnLedgerStatus.PROPOSED,
    )
}
