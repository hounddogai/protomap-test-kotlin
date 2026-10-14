package com.acme.transport.customs.clearance.clearancecheckpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched clearance checkpoint records of customs's clearance work, and the rules, stores, and
// calculations around them.

/** Where a batched clearance checkpoint is in its life cycle. */
enum class BatchedClearanceCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a batched clearance checkpoint and its current status. */
data class BatchedClearanceCheckpoint(
    var code: String,
    var label: String,
    var depot: String,
    var fuelLiters: Double,
    var rate: Double,
    var shiftCode: String,
    var amount: Double,
    var status: BatchedClearanceCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedClearanceCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedClearanceCheckpointStatus): Boolean {
        when (status) {
            BatchedClearanceCheckpointStatus.PROPOSED -> {
                when (next) {
                    BatchedClearanceCheckpointStatus.REJECTED, BatchedClearanceCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedClearanceCheckpointStatus.APPROVED -> return next == BatchedClearanceCheckpointStatus.REJECTED || next == BatchedClearanceCheckpointStatus.WITHDRAWN
            BatchedClearanceCheckpointStatus.REJECTED -> {
                when (next) {
                    BatchedClearanceCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedClearanceCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("batched clearance checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedVolume(): Double = round((amount - rate) * 100) / 100

    fun snapshot(): BatchedClearanceCheckpoint {
        return BatchedClearanceCheckpoint(
            code = code,
            label = label,
            depot = depot,
            fuelLiters = fuelLiters,
            rate = rate,
            shiftCode = shiftCode,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a batched clearance checkpoint in status proposed. */
fun newBatchedClearanceCheckpoint(code: String, label: String): BatchedClearanceCheckpoint {
    return BatchedClearanceCheckpoint(
        code = code,
        label = label,
        depot = "",
        fuelLiters = 0.0,
        rate = 0.0,
        shiftCode = "",
        amount = 0.0,
        status = BatchedClearanceCheckpointStatus.PROPOSED,
    )
}
