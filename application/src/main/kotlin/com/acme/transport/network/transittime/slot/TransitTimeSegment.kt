package com.acme.transport.network.transittime.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time segment is in its life cycle. */
enum class TransitTimeSegmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One transit time segment, with what the planners need to act on it. */
data class TransitTimeSegment(
    var code: String,
    var name: String,
    var maximum: Double,
    var channel: String,
    var attempts: Int,
    var status: TransitTimeSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeSegmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeSegmentStatus): Boolean {
        when (status) {
            TransitTimeSegmentStatus.CREATED -> return next == TransitTimeSegmentStatus.PUBLISHED || next == TransitTimeSegmentStatus.RETIRED
            TransitTimeSegmentStatus.VALIDATED -> return next == TransitTimeSegmentStatus.PUBLISHED || next == TransitTimeSegmentStatus.RETIRED
            TransitTimeSegmentStatus.PUBLISHED -> return next == TransitTimeSegmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TransitTimeSegmentStatus) {
        if (!canMoveTo(next)) {
            error("transit time segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round((attempts.toDouble() + maximum) * 100) / 100

    fun snapshot(): TransitTimeSegment {
        return TransitTimeSegment(
            code = code,
            name = name,
            maximum = maximum,
            channel = channel,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a transit time segment in status created. */
fun newTransitTimeSegment(code: String, name: String): TransitTimeSegment {
    return TransitTimeSegment(
        code = code,
        name = name,
        maximum = 0.0,
        channel = "",
        attempts = 0,
        status = TransitTimeSegmentStatus.CREATED,
    )
}
