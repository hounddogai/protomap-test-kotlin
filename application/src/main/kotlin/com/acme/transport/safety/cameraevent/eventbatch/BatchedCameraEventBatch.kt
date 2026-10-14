package com.acme.transport.safety.cameraevent.eventbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched camera event batch records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a batched camera event batch is in its life cycle. */
enum class BatchedCameraEventBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a batched camera event batch and its current status. */
data class BatchedCameraEventBatch(
    var code: String,
    var name: String,
    var threshold: Double,
    var durationMinutes: Int,
    var lane: String,
    var revision: Int,
    var rate: Double,
    var externalRef: String,
    var status: BatchedCameraEventBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCameraEventBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCameraEventBatchStatus): Boolean {
        when (status) {
            BatchedCameraEventBatchStatus.DRAFT -> return next == BatchedCameraEventBatchStatus.PENDING
            BatchedCameraEventBatchStatus.PENDING -> return next == BatchedCameraEventBatchStatus.ACTIVE
            BatchedCameraEventBatchStatus.ACTIVE -> {
                when (next) {
                    BatchedCameraEventBatchStatus.SUSPENDED, BatchedCameraEventBatchStatus.CLOSED -> return true
                }
                return false
            }
            BatchedCameraEventBatchStatus.SUSPENDED -> {
                when (next) {
                    BatchedCameraEventBatchStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedCameraEventBatchStatus) {
        if (!canMoveTo(next)) {
            error("batched camera event batch $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun weightedTotal(): Double = round(durationMinutes.toDouble() * threshold * 100) / 100

    fun snapshot(): BatchedCameraEventBatch {
        return BatchedCameraEventBatch(
            code = code,
            name = name,
            threshold = threshold,
            durationMinutes = durationMinutes,
            lane = lane,
            revision = revision,
            rate = rate,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched camera event batch in status draft. */
fun newBatchedCameraEventBatch(code: String, name: String): BatchedCameraEventBatch {
    return BatchedCameraEventBatch(
        code = code,
        name = name,
        threshold = 0.0,
        durationMinutes = 0,
        lane = "",
        revision = 0,
        rate = 0.0,
        externalRef = "",
        status = BatchedCameraEventBatchStatus.DRAFT,
    )
}
