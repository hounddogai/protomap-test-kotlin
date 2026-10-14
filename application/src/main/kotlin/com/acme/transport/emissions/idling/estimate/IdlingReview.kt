package com.acme.transport.emissions.idling.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling review is in its life cycle. */
enum class IdlingReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A idling review as the idling team keeps it. */
data class IdlingReview(
    var code: String,
    var title: String,
    var discount: Double,
    var country: String,
    var quantity: Int,
    var approved: Boolean,
    var status: IdlingReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingReviewStatus): Boolean {
        when (status) {
            IdlingReviewStatus.CREATED -> {
                when (next) {
                    IdlingReviewStatus.RETIRED -> return true
                }
                return false
            }
            IdlingReviewStatus.VALIDATED -> {
                when (next) {
                    IdlingReviewStatus.RETIRED -> return true
                }
                return false
            }
            IdlingReviewStatus.PUBLISHED -> return next == IdlingReviewStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: IdlingReviewStatus) {
        if (!canMoveTo(next)) {
            error("idling review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedExposure(): Double = round((quantity.toDouble() + discount) * 100) / 100

    fun snapshot(): IdlingReview {
        return IdlingReview(
            code = code,
            title = title,
            discount = discount,
            country = country,
            quantity = quantity,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a idling review in status created. */
fun newIdlingReview(code: String, title: String): IdlingReview {
    return IdlingReview(
        code = code,
        title = title,
        discount = 0.0,
        country = "",
        quantity = 0,
        approved = false,
        status = IdlingReviewStatus.CREATED,
    )
}
