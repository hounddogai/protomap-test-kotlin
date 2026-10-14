package com.acme.transport.lastmile.parcel.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional parcel review is in its life cycle. */
enum class RegionalParcelReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a regional parcel review and its current status. */
data class RegionalParcelReview(
    var code: String,
    var name: String,
    var reviewer: String,
    var depot: String,
    var attempts: Int,
    var quantity: Int,
    var expiresAt: Int,
    var phone: String,
    var status: RegionalParcelReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalParcelReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalParcelReviewStatus): Boolean {
        when (status) {
            RegionalParcelReviewStatus.CREATED -> {
                when (next) {
                    RegionalParcelReviewStatus.VALIDATED, RegionalParcelReviewStatus.PUBLISHED -> return true
                }
                return false
            }
            RegionalParcelReviewStatus.VALIDATED -> return next == RegionalParcelReviewStatus.PUBLISHED
            RegionalParcelReviewStatus.PUBLISHED -> return next == RegionalParcelReviewStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalParcelReviewStatus) {
        if (!canMoveTo(next)) {
            error("regional parcel review $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedTotal(): Double = round(attempts.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): RegionalParcelReview {
        return RegionalParcelReview(
            code = code,
            name = name,
            reviewer = reviewer,
            depot = depot,
            attempts = attempts,
            quantity = quantity,
            expiresAt = expiresAt,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a regional parcel review in status created. */
fun newRegionalParcelReview(code: String, name: String): RegionalParcelReview {
    return RegionalParcelReview(
        code = code,
        name = name,
        reviewer = "",
        depot = "",
        attempts = 0,
        quantity = 0,
        expiresAt = 0,
        phone = "",
        status = RegionalParcelReviewStatus.CREATED,
    )
}
