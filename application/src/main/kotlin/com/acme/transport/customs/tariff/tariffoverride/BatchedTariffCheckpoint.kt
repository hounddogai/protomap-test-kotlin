package com.acme.transport.customs.tariff.tariffoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched tariff checkpoint is in its life cycle. */
enum class BatchedTariffCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a batched tariff checkpoint and its current status. */
data class BatchedTariffCheckpoint(
    var code: String,
    var label: String,
    var startsAt: Int,
    var sequence: Int,
    var maximum: Double,
    var expiresAt: Int,
    var status: BatchedTariffCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTariffCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTariffCheckpointStatus): Boolean {
        when (status) {
            BatchedTariffCheckpointStatus.OPEN -> return next == BatchedTariffCheckpointStatus.ARCHIVED
            BatchedTariffCheckpointStatus.IN_REVIEW -> {
                when (next) {
                    BatchedTariffCheckpointStatus.RESOLVED -> return true
                }
                return false
            }
            BatchedTariffCheckpointStatus.RESOLVED -> return next == BatchedTariffCheckpointStatus.ARCHIVED
            BatchedTariffCheckpointStatus.REOPENED -> {
                when (next) {
                    BatchedTariffCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedTariffCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("batched tariff checkpoint $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun netTotal(): Double = round((maximum - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): BatchedTariffCheckpoint {
        return BatchedTariffCheckpoint(
            code = code,
            label = label,
            startsAt = startsAt,
            sequence = sequence,
            maximum = maximum,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched tariff checkpoint in status open. */
fun newBatchedTariffCheckpoint(code: String, label: String): BatchedTariffCheckpoint {
    return BatchedTariffCheckpoint(
        code = code,
        label = label,
        startsAt = 0,
        sequence = 0,
        maximum = 0.0,
        expiresAt = 0,
        status = BatchedTariffCheckpointStatus.OPEN,
    )
}
