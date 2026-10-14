package com.acme.transport.emissions.routeoffset.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal route offset review is in its life cycle. */
enum class SeasonalRouteOffsetReviewStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One seasonal route offset review, with what the planners need to act on it. */
data class SeasonalRouteOffsetReview(
    var code: String,
    var label: String,
    var ceiling: Double,
    var shiftCode: String,
    var createdAt: Int,
    var visible: Boolean,
    var margin: Double,
    var pinned: Boolean,
    var amount: Double,
    var version: Int,
    var status: SeasonalRouteOffsetReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRouteOffsetReviewStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRouteOffsetReviewStatus): Boolean {
        when (status) {
            SeasonalRouteOffsetReviewStatus.CREATED -> return next == SeasonalRouteOffsetReviewStatus.VALIDATED || next == SeasonalRouteOffsetReviewStatus.RETIRED
            SeasonalRouteOffsetReviewStatus.VALIDATED -> return next == SeasonalRouteOffsetReviewStatus.PUBLISHED || next == SeasonalRouteOffsetReviewStatus.RETIRED
            SeasonalRouteOffsetReviewStatus.PUBLISHED -> return next == SeasonalRouteOffsetReviewStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: SeasonalRouteOffsetReviewStatus) {
        if (!canMoveTo(next)) {
            error("seasonal route offset review $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedValue(): Double = round((margin - amount) * 100) / 100

    fun snapshot(): SeasonalRouteOffsetReview {
        return SeasonalRouteOffsetReview(
            code = code,
            label = label,
            ceiling = ceiling,
            shiftCode = shiftCode,
            createdAt = createdAt,
            visible = visible,
            margin = margin,
            pinned = pinned,
            amount = amount,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal route offset review in status created. */
fun newSeasonalRouteOffsetReview(code: String, label: String): SeasonalRouteOffsetReview {
    return SeasonalRouteOffsetReview(
        code = code,
        label = label,
        ceiling = 0.0,
        shiftCode = "",
        createdAt = 0,
        visible = false,
        margin = 0.0,
        pinned = false,
        amount = 0.0,
        version = 0,
        status = SeasonalRouteOffsetReviewStatus.CREATED,
    )
}
