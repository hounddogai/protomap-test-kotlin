package com.acme.transport.safety.scorecard.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard review is in its life cycle. */
enum class ScorecardReviewStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scorecard review. Its code is unique within a tenant. */
data class ScorecardReview(
    var code: String,
    var title: String,
    var threshold: Double,
    var score: Double,
    var distanceKm: Double,
    var version: Int,
    var approved: Boolean,
    var region: String,
    var floorValue: Double,
    var minimum: Double,
    var budget: Double,
    var status: ScorecardReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardReviewStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardReviewStatus): Boolean {
        when (status) {
            ScorecardReviewStatus.REQUESTED -> {
                when (next) {
                    ScorecardReviewStatus.REVOKED -> return true
                }
                return false
            }
            ScorecardReviewStatus.GRANTED -> {
                when (next) {
                    ScorecardReviewStatus.REVOKED, ScorecardReviewStatus.EXPIRED -> return true
                }
                return false
            }
            ScorecardReviewStatus.REVOKED -> {
                when (next) {
                    ScorecardReviewStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardReviewStatus) {
        if (!canMoveTo(next)) {
            error("scorecard review $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun projectedLoad(): Double = round((score - budget) * 100) / 100

    fun snapshot(): ScorecardReview {
        return ScorecardReview(
            code = code,
            title = title,
            threshold = threshold,
            score = score,
            distanceKm = distanceKm,
            version = version,
            approved = approved,
            region = region,
            floorValue = floorValue,
            minimum = minimum,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scorecard review in status requested. */
fun newScorecardReview(code: String, title: String): ScorecardReview {
    return ScorecardReview(
        code = code,
        title = title,
        threshold = 0.0,
        score = 0.0,
        distanceKm = 0.0,
        version = 0,
        approved = false,
        region = "",
        floorValue = 0.0,
        minimum = 0.0,
        budget = 0.0,
        status = ScorecardReviewStatus.REQUESTED,
    )
}
