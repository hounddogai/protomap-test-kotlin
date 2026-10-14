package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.kit.text.normalize

// The rate card review records of freightpay's rate card work, and the rules, stores, and calculations around
// them.

/** Where a rate card review is in its life cycle. */
enum class RateCardReviewStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rate card review. Its code is unique within a tenant. */
data class RateCardReview(
    var code: String,
    var title: String,
    var visible: Boolean,
    var dock: String,
    var limit: Int,
    var status: RateCardReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardReviewStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardReviewStatus): Boolean {
        when (status) {
            RateCardReviewStatus.PLANNED -> return next == RateCardReviewStatus.SCHEDULED
            RateCardReviewStatus.SCHEDULED -> return next == RateCardReviewStatus.CANCELLED
            RateCardReviewStatus.IN_PROGRESS -> {
                when (next) {
                    RateCardReviewStatus.COMPLETED, RateCardReviewStatus.CANCELLED -> return true
                }
                return false
            }
            RateCardReviewStatus.COMPLETED -> {
                when (next) {
                    RateCardReviewStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardReviewStatus) {
        if (!canMoveTo(next)) {
            error("rate card review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = limit.toDouble()

    fun snapshot(): RateCardReview {
        return RateCardReview(
            code = code,
            title = title,
            visible = visible,
            dock = dock,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rate card review in status planned. */
fun newRateCardReview(code: String, title: String): RateCardReview {
    return RateCardReview(
        code = code,
        title = title,
        visible = false,
        dock = "",
        limit = 0,
        status = RateCardReviewStatus.PLANNED,
    )
}
