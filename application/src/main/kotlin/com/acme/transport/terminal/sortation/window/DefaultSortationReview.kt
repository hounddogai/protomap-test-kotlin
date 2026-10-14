package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default sortation review is in its life cycle. */
enum class DefaultSortationReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a default sortation review and its current status. */
data class DefaultSortationReview(
    var code: String,
    var label: String,
    var weight: Double,
    var depot: String,
    var createdAt: Int,
    var status: DefaultSortationReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultSortationReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultSortationReviewStatus): Boolean {
        when (status) {
            DefaultSortationReviewStatus.IDLE -> {
                when (next) {
                    DefaultSortationReviewStatus.READY, DefaultSortationReviewStatus.DRAINING -> return true
                }
                return false
            }
            DefaultSortationReviewStatus.WARMING -> return next == DefaultSortationReviewStatus.READY
            DefaultSortationReviewStatus.READY -> return next == DefaultSortationReviewStatus.DRAINING || next == DefaultSortationReviewStatus.STOPPED
            DefaultSortationReviewStatus.DRAINING -> {
                when (next) {
                    DefaultSortationReviewStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultSortationReviewStatus) {
        if (!canMoveTo(next)) {
            error("default sortation review $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedExposure(): Double = round(weight * createdAt.toDouble() * 100) / 100

    fun snapshot(): DefaultSortationReview {
        return DefaultSortationReview(
            code = code,
            label = label,
            weight = weight,
            depot = depot,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default sortation review in status idle. */
fun newDefaultSortationReview(code: String, label: String): DefaultSortationReview {
    return DefaultSortationReview(
        code = code,
        label = label,
        weight = 0.0,
        depot = "",
        createdAt = 0,
        status = DefaultSortationReviewStatus.IDLE,
    )
}
