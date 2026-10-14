package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient route offset segment records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a lenient route offset segment is in its life cycle. */
enum class LenientRouteOffsetSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a lenient route offset segment and its current status. */
data class LenientRouteOffsetSegment(
    var code: String,
    var name: String,
    var ceiling: Double,
    var margin: Double,
    var locale: String,
    var pinned: Boolean,
    var status: LenientRouteOffsetSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientRouteOffsetSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientRouteOffsetSegmentStatus): Boolean {
        when (status) {
            LenientRouteOffsetSegmentStatus.IDLE -> {
                when (next) {
                    LenientRouteOffsetSegmentStatus.STOPPED -> return true
                }
                return false
            }
            LenientRouteOffsetSegmentStatus.WARMING -> {
                when (next) {
                    LenientRouteOffsetSegmentStatus.DRAINING -> return true
                }
                return false
            }
            LenientRouteOffsetSegmentStatus.READY -> return next == LenientRouteOffsetSegmentStatus.DRAINING
            LenientRouteOffsetSegmentStatus.DRAINING -> return next == LenientRouteOffsetSegmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LenientRouteOffsetSegmentStatus) {
        if (!canMoveTo(next)) {
            error("lenient route offset segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient route offset segment"} $code"

    fun netLoad(): Double = round((ceiling - margin) * 100) / 100

    fun snapshot(): LenientRouteOffsetSegment {
        return LenientRouteOffsetSegment(
            code = code,
            name = name,
            ceiling = ceiling,
            margin = margin,
            locale = locale,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a lenient route offset segment in status idle. */
fun newLenientRouteOffsetSegment(code: String, name: String): LenientRouteOffsetSegment {
    return LenientRouteOffsetSegment(
        code = code,
        name = name,
        ceiling = 0.0,
        margin = 0.0,
        locale = "",
        pinned = false,
        status = LenientRouteOffsetSegmentStatus.IDLE,
    )
}
