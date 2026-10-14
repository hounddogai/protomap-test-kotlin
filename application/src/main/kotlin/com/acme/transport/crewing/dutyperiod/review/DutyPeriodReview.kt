package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The duty period review records of crewing's duty period work, and the rules, stores, and calculations
// around them.

/** Where a duty period review is in its life cycle. */
enum class DutyPeriodReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A duty period review as the duty period team keeps it. */
data class DutyPeriodReview(
    var code: String,
    var name: String,
    var quantity: Int,
    var discount: Double,
    var currency: String,
    var country: String,
    var ratio: Double,
    var budget: Double,
    var status: DutyPeriodReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodReviewStatus): Boolean {
        when (status) {
            DutyPeriodReviewStatus.PROPOSED -> return next == DutyPeriodReviewStatus.REJECTED
            DutyPeriodReviewStatus.APPROVED -> {
                when (next) {
                    DutyPeriodReviewStatus.REJECTED -> return true
                }
                return false
            }
            DutyPeriodReviewStatus.REJECTED -> return next == DutyPeriodReviewStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DutyPeriodReviewStatus) {
        if (!canMoveTo(next)) {
            error("duty period review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"duty period review"} $code"

    fun weightedValue(): Double = round((discount + ratio) * 100) / 100

    fun snapshot(): DutyPeriodReview {
        return DutyPeriodReview(
            code = code,
            name = name,
            quantity = quantity,
            discount = discount,
            currency = currency,
            country = country,
            ratio = ratio,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a duty period review in status proposed. */
fun newDutyPeriodReview(code: String, name: String): DutyPeriodReview {
    return DutyPeriodReview(
        code = code,
        name = name,
        quantity = 0,
        discount = 0.0,
        currency = "",
        country = "",
        ratio = 0.0,
        budget = 0.0,
        status = DutyPeriodReviewStatus.PROPOSED,
    )
}
