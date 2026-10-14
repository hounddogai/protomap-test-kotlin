package com.acme.transport.crewing.roster.review

import com.acme.transport.kit.text.normalize

// The primary roster review records of crewing's roster work, and the rules, stores, and calculations around
// them.

/** Where a primary roster review is in its life cycle. */
enum class PrimaryRosterReviewStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A primary roster review as the roster team keeps it. */
data class PrimaryRosterReview(
    var code: String,
    var name: String,
    var score: Double,
    var lane: String,
    var unit: String,
    var contactEmail: String,
    var status: PrimaryRosterReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryRosterReviewStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryRosterReviewStatus): Boolean {
        when (status) {
            PrimaryRosterReviewStatus.REQUESTED -> {
                when (next) {
                    PrimaryRosterReviewStatus.EXPIRED -> return true
                }
                return false
            }
            PrimaryRosterReviewStatus.GRANTED -> return next == PrimaryRosterReviewStatus.REVOKED || next == PrimaryRosterReviewStatus.EXPIRED
            PrimaryRosterReviewStatus.REVOKED -> {
                when (next) {
                    PrimaryRosterReviewStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryRosterReviewStatus) {
        if (!canMoveTo(next)) {
            error("primary roster review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary roster review"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): PrimaryRosterReview {
        return PrimaryRosterReview(
            code = code,
            name = name,
            score = score,
            lane = lane,
            unit = unit,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a primary roster review in status requested. */
fun newPrimaryRosterReview(code: String, name: String): PrimaryRosterReview {
    return PrimaryRosterReview(
        code = code,
        name = name,
        score = 0.0,
        lane = "",
        unit = "",
        contactEmail = "",
        status = PrimaryRosterReviewStatus.REQUESTED,
    )
}
