package com.acme.transport.customs.inspection.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched inspection checkpoint is in its life cycle. */
enum class BatchedInspectionCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched inspection checkpoint. Its code is unique within a tenant. */
data class BatchedInspectionCheckpoint(
    var code: String,
    var title: String,
    var amount: Double,
    var externalRef: String,
    var archived: Boolean,
    var source: String,
    var windowDays: Int,
    var label: String,
    var weight: Double,
    var revision: Int,
    var status: BatchedInspectionCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedInspectionCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedInspectionCheckpointStatus): Boolean {
        when (status) {
            BatchedInspectionCheckpointStatus.PROPOSED -> return next == BatchedInspectionCheckpointStatus.REJECTED
            BatchedInspectionCheckpointStatus.APPROVED -> return next == BatchedInspectionCheckpointStatus.REJECTED || next == BatchedInspectionCheckpointStatus.WITHDRAWN
            BatchedInspectionCheckpointStatus.REJECTED -> return next == BatchedInspectionCheckpointStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedInspectionCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("batched inspection checkpoint $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"batched inspection checkpoint"} $code"

    fun weightedVolume(): Double = round(windowDays.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): BatchedInspectionCheckpoint {
        return BatchedInspectionCheckpoint(
            code = code,
            title = title,
            amount = amount,
            externalRef = externalRef,
            archived = archived,
            source = source,
            windowDays = windowDays,
            label = label,
            weight = weight,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched inspection checkpoint in status proposed. */
fun newBatchedInspectionCheckpoint(code: String, title: String): BatchedInspectionCheckpoint {
    return BatchedInspectionCheckpoint(
        code = code,
        title = title,
        amount = 0.0,
        externalRef = "",
        archived = false,
        source = "",
        windowDays = 0,
        label = "",
        weight = 0.0,
        revision = 0,
        status = BatchedInspectionCheckpointStatus.PROPOSED,
    )
}
