package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.text.normalize

/** Where a lenient accessorial review is in its life cycle. */
enum class LenientAccessorialReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A lenient accessorial review as the accessorial team keeps it. */
data class LenientAccessorialReview(
    var code: String,
    var name: String,
    var locale: String,
    var active: Boolean,
    var approved: Boolean,
    var cost: Double,
    var status: LenientAccessorialReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAccessorialReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAccessorialReviewStatus): Boolean {
        when (status) {
            LenientAccessorialReviewStatus.PROPOSED -> {
                when (next) {
                    LenientAccessorialReviewStatus.REJECTED -> return true
                }
                return false
            }
            LenientAccessorialReviewStatus.APPROVED -> return next == LenientAccessorialReviewStatus.WITHDRAWN
            LenientAccessorialReviewStatus.REJECTED -> return next == LenientAccessorialReviewStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LenientAccessorialReviewStatus) {
        if (!canMoveTo(next)) {
            error("lenient accessorial review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient accessorial review"} $code"

    fun measuredValue(): Double = cost

    fun snapshot(): LenientAccessorialReview {
        return LenientAccessorialReview(
            code = code,
            name = name,
            locale = locale,
            active = active,
            approved = approved,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lenient accessorial review in status proposed. */
fun newLenientAccessorialReview(code: String, name: String): LenientAccessorialReview {
    return LenientAccessorialReview(
        code = code,
        name = name,
        locale = "",
        active = false,
        approved = false,
        cost = 0.0,
        status = LenientAccessorialReviewStatus.PROPOSED,
    )
}
