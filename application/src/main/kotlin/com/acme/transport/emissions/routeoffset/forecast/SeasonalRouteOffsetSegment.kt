package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal route offset segment is in its life cycle. */
enum class SeasonalRouteOffsetSegmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a seasonal route offset segment and its current status. */
data class SeasonalRouteOffsetSegment(
    var code: String,
    var title: String,
    var attempts: Int,
    var lane: String,
    var trailerId: String,
    var threshold: Double,
    var status: SeasonalRouteOffsetSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRouteOffsetSegmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRouteOffsetSegmentStatus): Boolean {
        when (status) {
            SeasonalRouteOffsetSegmentStatus.REQUESTED -> return next == SeasonalRouteOffsetSegmentStatus.EXPIRED
            SeasonalRouteOffsetSegmentStatus.GRANTED -> return next == SeasonalRouteOffsetSegmentStatus.REVOKED || next == SeasonalRouteOffsetSegmentStatus.EXPIRED
            SeasonalRouteOffsetSegmentStatus.REVOKED -> {
                when (next) {
                    SeasonalRouteOffsetSegmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalRouteOffsetSegmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal route offset segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun effectiveExposure(): Double = round(threshold * attempts.toDouble() * 100) / 100

    fun snapshot(): SeasonalRouteOffsetSegment {
        return SeasonalRouteOffsetSegment(
            code = code,
            title = title,
            attempts = attempts,
            lane = lane,
            trailerId = trailerId,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a seasonal route offset segment in status requested. */
fun newSeasonalRouteOffsetSegment(code: String, title: String): SeasonalRouteOffsetSegment {
    return SeasonalRouteOffsetSegment(
        code = code,
        title = title,
        attempts = 0,
        lane = "",
        trailerId = "",
        threshold = 0.0,
        status = SeasonalRouteOffsetSegmentStatus.REQUESTED,
    )
}
