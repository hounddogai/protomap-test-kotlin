package com.acme.transport.linehaul.trailer.trailerbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched trailer batch records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a batched trailer batch is in its life cycle. */
enum class BatchedTrailerBatchStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a batched trailer batch and its current status. */
data class BatchedTrailerBatch(
    var code: String,
    var label: String,
    var channel: String,
    var segment: String,
    var driverId: String,
    var retries: Int,
    var cost: Double,
    var ownerId: String,
    var createdAt: Int,
    var status: BatchedTrailerBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTrailerBatchStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTrailerBatchStatus): Boolean {
        when (status) {
            BatchedTrailerBatchStatus.PROPOSED -> return next == BatchedTrailerBatchStatus.APPROVED || next == BatchedTrailerBatchStatus.WITHDRAWN
            BatchedTrailerBatchStatus.APPROVED -> return next == BatchedTrailerBatchStatus.REJECTED
            BatchedTrailerBatchStatus.REJECTED -> {
                when (next) {
                    BatchedTrailerBatchStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTrailerBatchStatus) {
        if (!canMoveTo(next)) {
            error("batched trailer batch $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"batched trailer batch"} $code"

    fun adjustedTotal(): Double = round((createdAt.toDouble() - cost) * 100) / 100

    fun snapshot(): BatchedTrailerBatch {
        return BatchedTrailerBatch(
            code = code,
            label = label,
            channel = channel,
            segment = segment,
            driverId = driverId,
            retries = retries,
            cost = cost,
            ownerId = ownerId,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a batched trailer batch in status proposed. */
fun newBatchedTrailerBatch(code: String, label: String): BatchedTrailerBatch {
    return BatchedTrailerBatch(
        code = code,
        label = label,
        channel = "",
        segment = "",
        driverId = "",
        retries = 0,
        cost = 0.0,
        ownerId = "",
        createdAt = 0,
        status = BatchedTrailerBatchStatus.PROPOSED,
    )
}
