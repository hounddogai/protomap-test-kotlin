package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched rest break review is in its life cycle. */
enum class BatchedRestBreakReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched rest break review as the rest break team keeps it. */
data class BatchedRestBreakReview(
    var code: String,
    var label: String,
    var timezone: String,
    var margin: Double,
    var createdAt: Int,
    var tags: MutableList<String>,
    var status: BatchedRestBreakReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRestBreakReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRestBreakReviewStatus): Boolean {
        when (status) {
            BatchedRestBreakReviewStatus.IDLE -> return next == BatchedRestBreakReviewStatus.STOPPED
            BatchedRestBreakReviewStatus.WARMING -> {
                when (next) {
                    BatchedRestBreakReviewStatus.DRAINING -> return true
                }
                return false
            }
            BatchedRestBreakReviewStatus.READY -> return next == BatchedRestBreakReviewStatus.STOPPED
            BatchedRestBreakReviewStatus.DRAINING -> {
                when (next) {
                    BatchedRestBreakReviewStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedRestBreakReviewStatus) {
        if (!canMoveTo(next)) {
            error("batched rest break review $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedExposure(): Double = round((margin + createdAt.toDouble()) * 100) / 100

    fun snapshot(): BatchedRestBreakReview {
        return BatchedRestBreakReview(
            code = code,
            label = label,
            timezone = timezone,
            margin = margin,
            createdAt = createdAt,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched rest break review in status idle. */
fun newBatchedRestBreakReview(code: String, label: String): BatchedRestBreakReview {
    return BatchedRestBreakReview(
        code = code,
        label = label,
        timezone = "",
        margin = 0.0,
        createdAt = 0,
        tags = mutableListOf<String>(),
        status = BatchedRestBreakReviewStatus.IDLE,
    )
}
