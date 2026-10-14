package com.acme.transport.freightpay.dispute.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dispute review records of freightpay's dispute work, and the rules, stores, and calculations around
// them.

/** Where a dispute review is in its life cycle. */
enum class DisputeReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a dispute review and its current status. */
data class DisputeReview(
    var code: String,
    var title: String,
    var depot: String,
    var graceDays: Int,
    var startsAt: Int,
    var summary: String,
    var createdAt: Int,
    var status: DisputeReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeReviewStatus): Boolean {
        when (status) {
            DisputeReviewStatus.PROPOSED -> {
                when (next) {
                    DisputeReviewStatus.REJECTED -> return true
                }
                return false
            }
            DisputeReviewStatus.APPROVED -> {
                when (next) {
                    DisputeReviewStatus.REJECTED, DisputeReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
            DisputeReviewStatus.REJECTED -> return next == DisputeReviewStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DisputeReviewStatus) {
        if (!canMoveTo(next)) {
            error("dispute review $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun projectedValue(): Double = round((createdAt.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): DisputeReview {
        return DisputeReview(
            code = code,
            title = title,
            depot = depot,
            graceDays = graceDays,
            startsAt = startsAt,
            summary = summary,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a dispute review in status proposed. */
fun newDisputeReview(code: String, title: String): DisputeReview {
    return DisputeReview(
        code = code,
        title = title,
        depot = "",
        graceDays = 0,
        startsAt = 0,
        summary = "",
        createdAt = 0,
        status = DisputeReviewStatus.PROPOSED,
    )
}
