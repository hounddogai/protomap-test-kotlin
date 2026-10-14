package com.acme.transport.crewing.qualification.tieredqualificationdigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched qualification checkpoint is in its life cycle. */
enum class BatchedQualificationCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched qualification checkpoint as the qualification team keeps it. */
data class BatchedQualificationCheckpoint(
    var code: String,
    var title: String,
    var unit: String,
    var score: Double,
    var source: String,
    var capacity: Int,
    var status: BatchedQualificationCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedQualificationCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedQualificationCheckpointStatus): Boolean {
        when (status) {
            BatchedQualificationCheckpointStatus.PROPOSED -> {
                when (next) {
                    BatchedQualificationCheckpointStatus.APPROVED, BatchedQualificationCheckpointStatus.REJECTED -> return true
                }
                return false
            }
            BatchedQualificationCheckpointStatus.APPROVED -> return next == BatchedQualificationCheckpointStatus.REJECTED
            BatchedQualificationCheckpointStatus.REJECTED -> {
                when (next) {
                    BatchedQualificationCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedQualificationCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("batched qualification checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched qualification checkpoint"} $code"

    fun projectedVolume(): Double = round((score + capacity.toDouble()) * 100) / 100

    fun snapshot(): BatchedQualificationCheckpoint {
        return BatchedQualificationCheckpoint(
            code = code,
            title = title,
            unit = unit,
            score = score,
            source = source,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a batched qualification checkpoint in status proposed. */
fun newBatchedQualificationCheckpoint(code: String, title: String): BatchedQualificationCheckpoint {
    return BatchedQualificationCheckpoint(
        code = code,
        title = title,
        unit = "",
        score = 0.0,
        source = "",
        capacity = 0,
        status = BatchedQualificationCheckpointStatus.PROPOSED,
    )
}
