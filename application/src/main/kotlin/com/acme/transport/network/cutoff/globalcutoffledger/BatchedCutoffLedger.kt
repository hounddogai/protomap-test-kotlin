package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched cutoff ledger records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a batched cutoff ledger is in its life cycle. */
enum class BatchedCutoffLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One batched cutoff ledger, with what the planners need to act on it. */
data class BatchedCutoffLedger(
    var code: String,
    var title: String,
    var unit: String,
    var quantity: Int,
    var margin: Double,
    var channel: String,
    var revision: Int,
    var carrier: String,
    var status: BatchedCutoffLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCutoffLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCutoffLedgerStatus): Boolean {
        when (status) {
            BatchedCutoffLedgerStatus.IDLE -> return next == BatchedCutoffLedgerStatus.WARMING
            BatchedCutoffLedgerStatus.WARMING -> return next == BatchedCutoffLedgerStatus.DRAINING
            BatchedCutoffLedgerStatus.READY -> {
                when (next) {
                    BatchedCutoffLedgerStatus.DRAINING -> return true
                }
                return false
            }
            BatchedCutoffLedgerStatus.DRAINING -> return next == BatchedCutoffLedgerStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedCutoffLedgerStatus) {
        if (!canMoveTo(next)) {
            error("batched cutoff ledger $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun netTotal(): Double = round(margin * quantity.toDouble() * 100) / 100

    fun snapshot(): BatchedCutoffLedger {
        return BatchedCutoffLedger(
            code = code,
            title = title,
            unit = unit,
            quantity = quantity,
            margin = margin,
            channel = channel,
            revision = revision,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a batched cutoff ledger in status idle. */
fun newBatchedCutoffLedger(code: String, title: String): BatchedCutoffLedger {
    return BatchedCutoffLedger(
        code = code,
        title = title,
        unit = "",
        quantity = 0,
        margin = 0.0,
        channel = "",
        revision = 0,
        carrier = "",
        status = BatchedCutoffLedgerStatus.IDLE,
    )
}
