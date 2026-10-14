package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a coaching review is in its life cycle. */
enum class CoachingReviewStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One coaching review, with what the planners need to act on it. */
data class CoachingReview(
    var code: String,
    var title: String,
    var currency: String,
    var pallets: Int,
    var sequence: Int,
    var status: CoachingReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingReviewStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingReviewStatus): Boolean {
        when (status) {
            CoachingReviewStatus.PLANNED -> return next == CoachingReviewStatus.SCHEDULED
            CoachingReviewStatus.SCHEDULED -> return next == CoachingReviewStatus.IN_PROGRESS || next == CoachingReviewStatus.COMPLETED
            CoachingReviewStatus.IN_PROGRESS -> return next == CoachingReviewStatus.COMPLETED || next == CoachingReviewStatus.CANCELLED
            CoachingReviewStatus.COMPLETED -> return next == CoachingReviewStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CoachingReviewStatus) {
        if (!canMoveTo(next)) {
            error("coaching review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"coaching review"} $code"

    fun adjustedExposure(): Double = round((sequence.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): CoachingReview {
        return CoachingReview(
            code = code,
            title = title,
            currency = currency,
            pallets = pallets,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a coaching review in status planned. */
fun newCoachingReview(code: String, title: String): CoachingReview {
    return CoachingReview(
        code = code,
        title = title,
        currency = "",
        pallets = 0,
        sequence = 0,
        status = CoachingReviewStatus.PLANNED,
    )
}
