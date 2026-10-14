package com.acme.transport.emissions.idling.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global idling ledger is in its life cycle. */
enum class GlobalIdlingLedgerStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A global idling ledger as the idling team keeps it. */
data class GlobalIdlingLedger(
    var code: String,
    var label: String,
    var priority: Int,
    var version: Int,
    var quantity: Int,
    var stopCount: Int,
    var lane: String,
    var retries: Int,
    var status: GlobalIdlingLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalIdlingLedgerStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalIdlingLedgerStatus): Boolean {
        when (status) {
            GlobalIdlingLedgerStatus.PROPOSED -> return next == GlobalIdlingLedgerStatus.REJECTED || next == GlobalIdlingLedgerStatus.WITHDRAWN
            GlobalIdlingLedgerStatus.APPROVED -> return next == GlobalIdlingLedgerStatus.REJECTED || next == GlobalIdlingLedgerStatus.WITHDRAWN
            GlobalIdlingLedgerStatus.REJECTED -> {
                when (next) {
                    GlobalIdlingLedgerStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalIdlingLedgerStatus) {
        if (!canMoveTo(next)) {
            error("global idling ledger $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"global idling ledger"} $code"

    fun weightedTotal(): Double = round((quantity.toDouble() + priority.toDouble()) * 100) / 100

    fun snapshot(): GlobalIdlingLedger {
        return GlobalIdlingLedger(
            code = code,
            label = label,
            priority = priority,
            version = version,
            quantity = quantity,
            stopCount = stopCount,
            lane = lane,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a global idling ledger in status proposed. */
fun newGlobalIdlingLedger(code: String, label: String): GlobalIdlingLedger {
    return GlobalIdlingLedger(
        code = code,
        label = label,
        priority = 0,
        version = 0,
        quantity = 0,
        stopCount = 0,
        lane = "",
        retries = 0,
        status = GlobalIdlingLedgerStatus.PROPOSED,
    )
}
