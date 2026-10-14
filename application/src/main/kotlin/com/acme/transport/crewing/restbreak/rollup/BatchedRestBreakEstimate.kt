package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.text.normalize

/** Where a batched rest break estimate is in its life cycle. */
enum class BatchedRestBreakEstimateStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a batched rest break estimate and its current status. */
data class BatchedRestBreakEstimate(
    var code: String,
    var title: String,
    var summary: String,
    var channel: String,
    var pinned: Boolean,
    var windowDays: Int,
    var country: String,
    var status: BatchedRestBreakEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRestBreakEstimateStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRestBreakEstimateStatus): Boolean {
        when (status) {
            BatchedRestBreakEstimateStatus.OPEN -> {
                when (next) {
                    BatchedRestBreakEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedRestBreakEstimateStatus.IN_REVIEW -> return next == BatchedRestBreakEstimateStatus.RESOLVED || next == BatchedRestBreakEstimateStatus.ARCHIVED
            BatchedRestBreakEstimateStatus.RESOLVED -> {
                when (next) {
                    BatchedRestBreakEstimateStatus.REOPENED, BatchedRestBreakEstimateStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedRestBreakEstimateStatus.REOPENED -> return next == BatchedRestBreakEstimateStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BatchedRestBreakEstimateStatus) {
        if (!canMoveTo(next)) {
            error("batched rest break estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): BatchedRestBreakEstimate {
        return BatchedRestBreakEstimate(
            code = code,
            title = title,
            summary = summary,
            channel = channel,
            pinned = pinned,
            windowDays = windowDays,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a batched rest break estimate in status open. */
fun newBatchedRestBreakEstimate(code: String, title: String): BatchedRestBreakEstimate {
    return BatchedRestBreakEstimate(
        code = code,
        title = title,
        summary = "",
        channel = "",
        pinned = false,
        windowDays = 0,
        country = "",
        status = BatchedRestBreakEstimateStatus.OPEN,
    )
}
