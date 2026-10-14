package com.acme.transport.emissions.routeoffset.offsetnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict route offset segment is in its life cycle. */
enum class StrictRouteOffsetSegmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One strict route offset segment, with what the planners need to act on it. */
data class StrictRouteOffsetSegment(
    var code: String,
    var label: String,
    var endsAt: Int,
    var cost: Double,
    var lane: String,
    var currency: String,
    var maximum: Double,
    var status: StrictRouteOffsetSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRouteOffsetSegmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRouteOffsetSegmentStatus): Boolean {
        when (status) {
            StrictRouteOffsetSegmentStatus.DRAFT -> {
                when (next) {
                    StrictRouteOffsetSegmentStatus.PENDING, StrictRouteOffsetSegmentStatus.ACTIVE -> return true
                }
                return false
            }
            StrictRouteOffsetSegmentStatus.PENDING -> {
                when (next) {
                    StrictRouteOffsetSegmentStatus.ACTIVE, StrictRouteOffsetSegmentStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictRouteOffsetSegmentStatus.ACTIVE -> return next == StrictRouteOffsetSegmentStatus.SUSPENDED || next == StrictRouteOffsetSegmentStatus.CLOSED
            StrictRouteOffsetSegmentStatus.SUSPENDED -> return next == StrictRouteOffsetSegmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: StrictRouteOffsetSegmentStatus) {
        if (!canMoveTo(next)) {
            error("strict route offset segment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun adjustedExposure(): Double = round(cost * endsAt.toDouble() * 100) / 100

    fun snapshot(): StrictRouteOffsetSegment {
        return StrictRouteOffsetSegment(
            code = code,
            label = label,
            endsAt = endsAt,
            cost = cost,
            lane = lane,
            currency = currency,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict route offset segment in status draft. */
fun newStrictRouteOffsetSegment(code: String, label: String): StrictRouteOffsetSegment {
    return StrictRouteOffsetSegment(
        code = code,
        label = label,
        endsAt = 0,
        cost = 0.0,
        lane = "",
        currency = "",
        maximum = 0.0,
        status = StrictRouteOffsetSegmentStatus.DRAFT,
    )
}
