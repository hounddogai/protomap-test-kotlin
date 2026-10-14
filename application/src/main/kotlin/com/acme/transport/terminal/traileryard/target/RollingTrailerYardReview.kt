package com.acme.transport.terminal.traileryard.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling trailer yard review is in its life cycle. */
enum class RollingTrailerYardReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rolling trailer yard review as the trailer yard team keeps it. */
data class RollingTrailerYardReview(
    var code: String,
    var name: String,
    var amount: Double,
    var currency: String,
    var quantity: Int,
    var carrier: String,
    var unit: String,
    var status: RollingTrailerYardReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTrailerYardReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTrailerYardReviewStatus): Boolean {
        when (status) {
            RollingTrailerYardReviewStatus.PROPOSED -> return next == RollingTrailerYardReviewStatus.APPROVED
            RollingTrailerYardReviewStatus.APPROVED -> return next == RollingTrailerYardReviewStatus.REJECTED
            RollingTrailerYardReviewStatus.REJECTED -> return next == RollingTrailerYardReviewStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingTrailerYardReviewStatus) {
        if (!canMoveTo(next)) {
            error("rolling trailer yard review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedValue(): Double = round(amount * quantity.toDouble() * 100) / 100

    fun snapshot(): RollingTrailerYardReview {
        return RollingTrailerYardReview(
            code = code,
            name = name,
            amount = amount,
            currency = currency,
            quantity = quantity,
            carrier = carrier,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a rolling trailer yard review in status proposed. */
fun newRollingTrailerYardReview(code: String, name: String): RollingTrailerYardReview {
    return RollingTrailerYardReview(
        code = code,
        name = name,
        amount = 0.0,
        currency = "",
        quantity = 0,
        carrier = "",
        unit = "",
        status = RollingTrailerYardReviewStatus.PROPOSED,
    )
}
