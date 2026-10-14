package com.acme.transport.emissions.routeoffset.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback route offset review is in its life cycle. */
enum class FallbackRouteOffsetReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One fallback route offset review, with what the planners need to act on it. */
data class FallbackRouteOffsetReview(
    var code: String,
    var name: String,
    var summary: String,
    var unit: String,
    var distanceKm: Double,
    var ratio: Double,
    var timezone: String,
    var reviewer: String,
    var ownerId: String,
    var graceDays: Int,
    var amount: Double,
    var status: FallbackRouteOffsetReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRouteOffsetReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRouteOffsetReviewStatus): Boolean {
        when (status) {
            FallbackRouteOffsetReviewStatus.IDLE -> {
                when (next) {
                    FallbackRouteOffsetReviewStatus.READY, FallbackRouteOffsetReviewStatus.STOPPED -> return true
                }
                return false
            }
            FallbackRouteOffsetReviewStatus.WARMING -> {
                when (next) {
                    FallbackRouteOffsetReviewStatus.DRAINING, FallbackRouteOffsetReviewStatus.STOPPED -> return true
                }
                return false
            }
            FallbackRouteOffsetReviewStatus.READY -> return next == FallbackRouteOffsetReviewStatus.DRAINING || next == FallbackRouteOffsetReviewStatus.STOPPED
            FallbackRouteOffsetReviewStatus.DRAINING -> return next == FallbackRouteOffsetReviewStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FallbackRouteOffsetReviewStatus) {
        if (!canMoveTo(next)) {
            error("fallback route offset review $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedVolume(): Double = round(graceDays.toDouble() * distanceKm * 100) / 100

    fun snapshot(): FallbackRouteOffsetReview {
        return FallbackRouteOffsetReview(
            code = code,
            name = name,
            summary = summary,
            unit = unit,
            distanceKm = distanceKm,
            ratio = ratio,
            timezone = timezone,
            reviewer = reviewer,
            ownerId = ownerId,
            graceDays = graceDays,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback route offset review in status idle. */
fun newFallbackRouteOffsetReview(code: String, name: String): FallbackRouteOffsetReview {
    return FallbackRouteOffsetReview(
        code = code,
        name = name,
        summary = "",
        unit = "",
        distanceKm = 0.0,
        ratio = 0.0,
        timezone = "",
        reviewer = "",
        ownerId = "",
        graceDays = 0,
        amount = 0.0,
        status = FallbackRouteOffsetReviewStatus.IDLE,
    )
}
