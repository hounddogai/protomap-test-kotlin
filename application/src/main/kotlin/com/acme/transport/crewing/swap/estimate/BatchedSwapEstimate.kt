package com.acme.transport.crewing.swap.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched swap estimate records of crewing's swap work, and the rules, stores, and calculations around
// them.

/** Where a batched swap estimate is in its life cycle. */
enum class BatchedSwapEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a batched swap estimate and its current status. */
data class BatchedSwapEstimate(
    var code: String,
    var label: String,
    var startsAt: Int,
    var archived: Boolean,
    var etaMinutes: Int,
    var cost: Double,
    var retries: Int,
    var vehicleId: String,
    var graceDays: Int,
    var pinned: Boolean,
    var status: BatchedSwapEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedSwapEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedSwapEstimateStatus): Boolean {
        when (status) {
            BatchedSwapEstimateStatus.OPEN -> {
                when (next) {
                    BatchedSwapEstimateStatus.RESOLVED -> return true
                }
                return false
            }
            BatchedSwapEstimateStatus.IN_REVIEW -> return next == BatchedSwapEstimateStatus.RESOLVED || next == BatchedSwapEstimateStatus.ARCHIVED
            BatchedSwapEstimateStatus.RESOLVED -> return next == BatchedSwapEstimateStatus.REOPENED || next == BatchedSwapEstimateStatus.ARCHIVED
            BatchedSwapEstimateStatus.REOPENED -> {
                when (next) {
                    BatchedSwapEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedSwapEstimateStatus) {
        if (!canMoveTo(next)) {
            error("batched swap estimate $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedValue(): Double = round(startsAt.toDouble() * cost * 100) / 100

    fun snapshot(): BatchedSwapEstimate {
        return BatchedSwapEstimate(
            code = code,
            label = label,
            startsAt = startsAt,
            archived = archived,
            etaMinutes = etaMinutes,
            cost = cost,
            retries = retries,
            vehicleId = vehicleId,
            graceDays = graceDays,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched swap estimate in status open. */
fun newBatchedSwapEstimate(code: String, label: String): BatchedSwapEstimate {
    return BatchedSwapEstimate(
        code = code,
        label = label,
        startsAt = 0,
        archived = false,
        etaMinutes = 0,
        cost = 0.0,
        retries = 0,
        vehicleId = "",
        graceDays = 0,
        pinned = false,
        status = BatchedSwapEstimateStatus.OPEN,
    )
}
