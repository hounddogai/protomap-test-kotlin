package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched dispute estimate is in its life cycle. */
enum class BatchedDisputeEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched dispute estimate as the dispute team keeps it. */
data class BatchedDisputeEstimate(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var carrier: String,
    var ceiling: Double,
    var externalRef: String,
    var trailerId: String,
    var vehicleId: String,
    var version: Int,
    var status: BatchedDisputeEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDisputeEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDisputeEstimateStatus): Boolean {
        when (status) {
            BatchedDisputeEstimateStatus.OPEN -> return next == BatchedDisputeEstimateStatus.REOPENED
            BatchedDisputeEstimateStatus.IN_REVIEW -> return next == BatchedDisputeEstimateStatus.REOPENED || next == BatchedDisputeEstimateStatus.ARCHIVED
            BatchedDisputeEstimateStatus.RESOLVED -> {
                when (next) {
                    BatchedDisputeEstimateStatus.REOPENED -> return true
                }
                return false
            }
            BatchedDisputeEstimateStatus.REOPENED -> return next == BatchedDisputeEstimateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BatchedDisputeEstimateStatus) {
        if (!canMoveTo(next)) {
            error("batched dispute estimate $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"batched dispute estimate"} $code"

    fun weightedValue(): Double = round((version.toDouble() - ceiling) * 100) / 100

    fun snapshot(): BatchedDisputeEstimate {
        return BatchedDisputeEstimate(
            code = code,
            name = name,
            pinned = pinned,
            carrier = carrier,
            ceiling = ceiling,
            externalRef = externalRef,
            trailerId = trailerId,
            vehicleId = vehicleId,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched dispute estimate in status open. */
fun newBatchedDisputeEstimate(code: String, name: String): BatchedDisputeEstimate {
    return BatchedDisputeEstimate(
        code = code,
        name = name,
        pinned = false,
        carrier = "",
        ceiling = 0.0,
        externalRef = "",
        trailerId = "",
        vehicleId = "",
        version = 0,
        status = BatchedDisputeEstimateStatus.OPEN,
    )
}
