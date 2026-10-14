package com.acme.transport.crewing.roster.rostersummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster review is in its life cycle. */
enum class RosterReviewStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a roster review and its current status. */
data class RosterReview(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var budget: Double,
    var country: String,
    var timezone: String,
    var status: RosterReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterReviewStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterReviewStatus): Boolean {
        when (status) {
            RosterReviewStatus.PROPOSED -> {
                when (next) {
                    RosterReviewStatus.REJECTED -> return true
                }
                return false
            }
            RosterReviewStatus.APPROVED -> {
                when (next) {
                    RosterReviewStatus.WITHDRAWN -> return true
                }
                return false
            }
            RosterReviewStatus.REJECTED -> return next == RosterReviewStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RosterReviewStatus) {
        if (!canMoveTo(next)) {
            error("roster review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"roster review"} $code"

    fun netLoad(): Double = round((distanceKm - budget) * 100) / 100

    fun snapshot(): RosterReview {
        return RosterReview(
            code = code,
            name = name,
            distanceKm = distanceKm,
            budget = budget,
            country = country,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a roster review in status proposed. */
fun newRosterReview(code: String, name: String): RosterReview {
    return RosterReview(
        code = code,
        name = name,
        distanceKm = 0.0,
        budget = 0.0,
        country = "",
        timezone = "",
        status = RosterReviewStatus.PROPOSED,
    )
}
