package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy cross dock ledger records of terminal's cross dock work, and the rules, stores, and calculations
// around them.

/** Where a legacy cross dock ledger is in its life cycle. */
enum class LegacyCrossDockLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A legacy cross dock ledger as the cross dock team keeps it. */
data class LegacyCrossDockLedger(
    var code: String,
    var name: String,
    var notes: String,
    var etaMinutes: Int,
    var minimum: Double,
    var expiresAt: Int,
    var depot: String,
    var status: LegacyCrossDockLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCrossDockLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCrossDockLedgerStatus): Boolean {
        when (status) {
            LegacyCrossDockLedgerStatus.PROPOSED -> return next == LegacyCrossDockLedgerStatus.APPROVED
            LegacyCrossDockLedgerStatus.APPROVED -> return next == LegacyCrossDockLedgerStatus.REJECTED
            LegacyCrossDockLedgerStatus.REJECTED -> return next == LegacyCrossDockLedgerStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyCrossDockLedgerStatus) {
        if (!canMoveTo(next)) {
            error("legacy cross dock ledger $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedValue(): Double = round((expiresAt.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): LegacyCrossDockLedger {
        return LegacyCrossDockLedger(
            code = code,
            name = name,
            notes = notes,
            etaMinutes = etaMinutes,
            minimum = minimum,
            expiresAt = expiresAt,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy cross dock ledger in status proposed. */
fun newLegacyCrossDockLedger(code: String, name: String): LegacyCrossDockLedger {
    return LegacyCrossDockLedger(
        code = code,
        name = name,
        notes = "",
        etaMinutes = 0,
        minimum = 0.0,
        expiresAt = 0,
        depot = "",
        status = LegacyCrossDockLedgerStatus.PROPOSED,
    )
}
