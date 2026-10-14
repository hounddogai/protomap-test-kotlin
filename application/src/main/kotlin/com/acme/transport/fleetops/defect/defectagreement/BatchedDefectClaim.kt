package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched defect claim is in its life cycle. */
enum class BatchedDefectClaimStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a batched defect claim and its current status. */
data class BatchedDefectClaim(
    var code: String,
    var label: String,
    var carrier: String,
    var maximum: Double,
    var attempts: Int,
    var status: BatchedDefectClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDefectClaimStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDefectClaimStatus): Boolean {
        when (status) {
            BatchedDefectClaimStatus.PLANNED -> {
                when (next) {
                    BatchedDefectClaimStatus.IN_PROGRESS, BatchedDefectClaimStatus.COMPLETED -> return true
                }
                return false
            }
            BatchedDefectClaimStatus.SCHEDULED -> {
                when (next) {
                    BatchedDefectClaimStatus.COMPLETED, BatchedDefectClaimStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedDefectClaimStatus.IN_PROGRESS -> return next == BatchedDefectClaimStatus.COMPLETED || next == BatchedDefectClaimStatus.CANCELLED
            BatchedDefectClaimStatus.COMPLETED -> return next == BatchedDefectClaimStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BatchedDefectClaimStatus) {
        if (!canMoveTo(next)) {
            error("batched defect claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedTotal(): Double = round((maximum - attempts.toDouble()) * 100) / 100

    fun snapshot(): BatchedDefectClaim {
        return BatchedDefectClaim(
            code = code,
            label = label,
            carrier = carrier,
            maximum = maximum,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched defect claim in status planned. */
fun newBatchedDefectClaim(code: String, label: String): BatchedDefectClaim {
    return BatchedDefectClaim(
        code = code,
        label = label,
        carrier = "",
        maximum = 0.0,
        attempts = 0,
        status = BatchedDefectClaimStatus.PLANNED,
    )
}
