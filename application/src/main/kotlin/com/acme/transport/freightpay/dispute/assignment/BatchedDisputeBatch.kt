package com.acme.transport.freightpay.dispute.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched dispute batch is in its life cycle. */
enum class BatchedDisputeBatchStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A batched dispute batch as the dispute team keeps it. */
data class BatchedDisputeBatch(
    var code: String,
    var label: String,
    var amount: Double,
    var maximum: Double,
    var sequence: Int,
    var status: BatchedDisputeBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDisputeBatchStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDisputeBatchStatus): Boolean {
        when (status) {
            BatchedDisputeBatchStatus.PLANNED -> {
                when (next) {
                    BatchedDisputeBatchStatus.IN_PROGRESS, BatchedDisputeBatchStatus.COMPLETED -> return true
                }
                return false
            }
            BatchedDisputeBatchStatus.SCHEDULED -> return next == BatchedDisputeBatchStatus.IN_PROGRESS || next == BatchedDisputeBatchStatus.CANCELLED
            BatchedDisputeBatchStatus.IN_PROGRESS -> return next == BatchedDisputeBatchStatus.COMPLETED || next == BatchedDisputeBatchStatus.CANCELLED
            BatchedDisputeBatchStatus.COMPLETED -> return next == BatchedDisputeBatchStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BatchedDisputeBatchStatus) {
        if (!canMoveTo(next)) {
            error("batched dispute batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"batched dispute batch"} $code"

    fun adjustedLoad(): Double = round(amount * sequence.toDouble() * 100) / 100

    fun snapshot(): BatchedDisputeBatch {
        return BatchedDisputeBatch(
            code = code,
            label = label,
            amount = amount,
            maximum = maximum,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched dispute batch in status planned. */
fun newBatchedDisputeBatch(code: String, label: String): BatchedDisputeBatch {
    return BatchedDisputeBatch(
        code = code,
        label = label,
        amount = 0.0,
        maximum = 0.0,
        sequence = 0,
        status = BatchedDisputeBatchStatus.PLANNED,
    )
}
