package com.acme.transport.crewing.swap.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched swap variance is in its life cycle. */
enum class BatchedSwapVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One batched swap variance, with what the planners need to act on it. */
data class BatchedSwapVariance(
    var code: String,
    var name: String,
    var capacity: Int,
    var minimum: Double,
    var stopCount: Int,
    var shiftCode: String,
    var status: BatchedSwapVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedSwapVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedSwapVarianceStatus): Boolean {
        when (status) {
            BatchedSwapVarianceStatus.OPEN -> return next == BatchedSwapVarianceStatus.IN_REVIEW
            BatchedSwapVarianceStatus.IN_REVIEW -> {
                when (next) {
                    BatchedSwapVarianceStatus.REOPENED -> return true
                }
                return false
            }
            BatchedSwapVarianceStatus.RESOLVED -> {
                when (next) {
                    BatchedSwapVarianceStatus.REOPENED, BatchedSwapVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedSwapVarianceStatus.REOPENED -> {
                when (next) {
                    BatchedSwapVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedSwapVarianceStatus) {
        if (!canMoveTo(next)) {
            error("batched swap variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched swap variance"} $code"

    fun weightedVolume(): Double = round((minimum - capacity.toDouble()) * 100) / 100

    fun snapshot(): BatchedSwapVariance {
        return BatchedSwapVariance(
            code = code,
            name = name,
            capacity = capacity,
            minimum = minimum,
            stopCount = stopCount,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched swap variance in status open. */
fun newBatchedSwapVariance(code: String, name: String): BatchedSwapVariance {
    return BatchedSwapVariance(
        code = code,
        name = name,
        capacity = 0,
        minimum = 0.0,
        stopCount = 0,
        shiftCode = "",
        status = BatchedSwapVarianceStatus.OPEN,
    )
}
