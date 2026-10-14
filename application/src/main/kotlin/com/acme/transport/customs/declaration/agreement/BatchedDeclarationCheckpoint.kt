package com.acme.transport.customs.declaration.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched declaration checkpoint is in its life cycle. */
enum class BatchedDeclarationCheckpointStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A batched declaration checkpoint. Its code is unique within a tenant. */
data class BatchedDeclarationCheckpoint(
    var code: String,
    var name: String,
    var limit: Int,
    var floorValue: Double,
    var amount: Double,
    var rate: Double,
    var revision: Int,
    var discount: Double,
    var channel: String,
    var status: BatchedDeclarationCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDeclarationCheckpointStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDeclarationCheckpointStatus): Boolean {
        when (status) {
            BatchedDeclarationCheckpointStatus.PLANNED -> {
                when (next) {
                    BatchedDeclarationCheckpointStatus.IN_PROGRESS, BatchedDeclarationCheckpointStatus.COMPLETED -> return true
                }
                return false
            }
            BatchedDeclarationCheckpointStatus.SCHEDULED -> return next == BatchedDeclarationCheckpointStatus.IN_PROGRESS
            BatchedDeclarationCheckpointStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedDeclarationCheckpointStatus.COMPLETED -> return true
                }
                return false
            }
            BatchedDeclarationCheckpointStatus.COMPLETED -> return next == BatchedDeclarationCheckpointStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BatchedDeclarationCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("batched declaration checkpoint $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round(amount * revision.toDouble() * 100) / 100

    fun snapshot(): BatchedDeclarationCheckpoint {
        return BatchedDeclarationCheckpoint(
            code = code,
            name = name,
            limit = limit,
            floorValue = floorValue,
            amount = amount,
            rate = rate,
            revision = revision,
            discount = discount,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a batched declaration checkpoint in status planned. */
fun newBatchedDeclarationCheckpoint(code: String, name: String): BatchedDeclarationCheckpoint {
    return BatchedDeclarationCheckpoint(
        code = code,
        name = name,
        limit = 0,
        floorValue = 0.0,
        amount = 0.0,
        rate = 0.0,
        revision = 0,
        discount = 0.0,
        channel = "",
        status = BatchedDeclarationCheckpointStatus.PLANNED,
    )
}
