package com.acme.transport.network.transittime.mapping

import com.acme.transport.kit.text.normalize

/** Where a seasonal transit time review is in its life cycle. */
enum class SeasonalTransitTimeReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One seasonal transit time review, with what the planners need to act on it. */
data class SeasonalTransitTimeReview(
    var code: String,
    var title: String,
    var depot: String,
    var label: String,
    var active: Boolean,
    var score: Double,
    var status: SeasonalTransitTimeReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTransitTimeReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTransitTimeReviewStatus): Boolean {
        when (status) {
            SeasonalTransitTimeReviewStatus.CREATED -> return next == SeasonalTransitTimeReviewStatus.RETIRED
            SeasonalTransitTimeReviewStatus.VALIDATED -> return next == SeasonalTransitTimeReviewStatus.PUBLISHED || next == SeasonalTransitTimeReviewStatus.RETIRED
            SeasonalTransitTimeReviewStatus.PUBLISHED -> return next == SeasonalTransitTimeReviewStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SeasonalTransitTimeReviewStatus) {
        if (!canMoveTo(next)) {
            error("seasonal transit time review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($title)"

    fun measuredValue(): Double = score

    fun snapshot(): SeasonalTransitTimeReview {
        return SeasonalTransitTimeReview(
            code = code,
            title = title,
            depot = depot,
            label = label,
            active = active,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a seasonal transit time review in status created. */
fun newSeasonalTransitTimeReview(code: String, title: String): SeasonalTransitTimeReview {
    return SeasonalTransitTimeReview(
        code = code,
        title = title,
        depot = "",
        label = "",
        active = false,
        score = 0.0,
        status = SeasonalTransitTimeReviewStatus.CREATED,
    )
}
