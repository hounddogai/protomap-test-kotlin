package com.acme.transport.emissions.routeoffset.offsetrule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback route offset score is in its life cycle. */
enum class FallbackRouteOffsetScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One fallback route offset score, with what the planners need to act on it. */
data class FallbackRouteOffsetScore(
    var code: String,
    var title: String,
    var attempts: Int,
    var minimum: Double,
    var lane: String,
    var routeCode: String,
    var etaMinutes: Int,
    var windowDays: Int,
    var startsAt: Int,
    var status: FallbackRouteOffsetScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRouteOffsetScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRouteOffsetScoreStatus): Boolean {
        when (status) {
            FallbackRouteOffsetScoreStatus.PLANNED -> return next == FallbackRouteOffsetScoreStatus.SCHEDULED || next == FallbackRouteOffsetScoreStatus.IN_PROGRESS
            FallbackRouteOffsetScoreStatus.SCHEDULED -> return next == FallbackRouteOffsetScoreStatus.IN_PROGRESS || next == FallbackRouteOffsetScoreStatus.COMPLETED
            FallbackRouteOffsetScoreStatus.IN_PROGRESS -> {
                when (next) {
                    FallbackRouteOffsetScoreStatus.COMPLETED, FallbackRouteOffsetScoreStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackRouteOffsetScoreStatus.COMPLETED -> {
                when (next) {
                    FallbackRouteOffsetScoreStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackRouteOffsetScoreStatus) {
        if (!canMoveTo(next)) {
            error("fallback route offset score $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback route offset score"} $code"

    fun weightedExposure(): Double = round((startsAt.toDouble() + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): FallbackRouteOffsetScore {
        return FallbackRouteOffsetScore(
            code = code,
            title = title,
            attempts = attempts,
            minimum = minimum,
            lane = lane,
            routeCode = routeCode,
            etaMinutes = etaMinutes,
            windowDays = windowDays,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fallback route offset score in status planned. */
fun newFallbackRouteOffsetScore(code: String, title: String): FallbackRouteOffsetScore {
    return FallbackRouteOffsetScore(
        code = code,
        title = title,
        attempts = 0,
        minimum = 0.0,
        lane = "",
        routeCode = "",
        etaMinutes = 0,
        windowDays = 0,
        startsAt = 0,
        status = FallbackRouteOffsetScoreStatus.PLANNED,
    )
}
