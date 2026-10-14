package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.text.normalize

// The trailer yard review records of terminal's trailer yard work, and the rules, stores, and calculations
// around them.

/** Where a trailer yard review is in its life cycle. */
enum class TrailerYardReviewStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A trailer yard review. Its code is unique within a tenant. */
data class TrailerYardReview(
    var code: String,
    var label: String,
    var locale: String,
    var fuelLiters: Double,
    var segment: String,
    var name: String,
    var timezone: String,
    var status: TrailerYardReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardReviewStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardReviewStatus): Boolean {
        when (status) {
            TrailerYardReviewStatus.OPEN -> return next == TrailerYardReviewStatus.ARCHIVED
            TrailerYardReviewStatus.IN_REVIEW -> {
                when (next) {
                    TrailerYardReviewStatus.ARCHIVED -> return true
                }
                return false
            }
            TrailerYardReviewStatus.RESOLVED -> {
                when (next) {
                    TrailerYardReviewStatus.REOPENED -> return true
                }
                return false
            }
            TrailerYardReviewStatus.REOPENED -> return next == TrailerYardReviewStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: TrailerYardReviewStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"trailer yard review"} $code"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): TrailerYardReview {
        return TrailerYardReview(
            code = code,
            label = label,
            locale = locale,
            fuelLiters = fuelLiters,
            segment = segment,
            name = name,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a trailer yard review in status open. */
fun newTrailerYardReview(code: String, label: String): TrailerYardReview {
    return TrailerYardReview(
        code = code,
        label = label,
        locale = "",
        fuelLiters = 0.0,
        segment = "",
        name = "",
        timezone = "",
        status = TrailerYardReviewStatus.OPEN,
    )
}
