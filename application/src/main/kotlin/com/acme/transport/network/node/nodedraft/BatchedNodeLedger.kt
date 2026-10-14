package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched node ledger is in its life cycle. */
enum class BatchedNodeLedgerStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a batched node ledger and its current status. */
data class BatchedNodeLedger(
    var code: String,
    var title: String,
    var discount: Double,
    var reviewer: String,
    var phone: String,
    var depot: String,
    var routeCode: String,
    var stopCount: Int,
    var status: BatchedNodeLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedNodeLedgerStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedNodeLedgerStatus): Boolean {
        when (status) {
            BatchedNodeLedgerStatus.PLANNED -> return next == BatchedNodeLedgerStatus.IN_PROGRESS || next == BatchedNodeLedgerStatus.COMPLETED
            BatchedNodeLedgerStatus.SCHEDULED -> {
                when (next) {
                    BatchedNodeLedgerStatus.IN_PROGRESS -> return true
                }
                return false
            }
            BatchedNodeLedgerStatus.IN_PROGRESS -> return next == BatchedNodeLedgerStatus.COMPLETED
            BatchedNodeLedgerStatus.COMPLETED -> {
                when (next) {
                    BatchedNodeLedgerStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedNodeLedgerStatus) {
        if (!canMoveTo(next)) {
            error("batched node ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched node ledger"} $code"

    fun adjustedValue(): Double = round((stopCount.toDouble() + discount) * 100) / 100

    fun snapshot(): BatchedNodeLedger {
        return BatchedNodeLedger(
            code = code,
            title = title,
            discount = discount,
            reviewer = reviewer,
            phone = phone,
            depot = depot,
            routeCode = routeCode,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a batched node ledger in status planned. */
fun newBatchedNodeLedger(code: String, title: String): BatchedNodeLedger {
    return BatchedNodeLedger(
        code = code,
        title = title,
        discount = 0.0,
        reviewer = "",
        phone = "",
        depot = "",
        routeCode = "",
        stopCount = 0,
        status = BatchedNodeLedgerStatus.PLANNED,
    )
}
