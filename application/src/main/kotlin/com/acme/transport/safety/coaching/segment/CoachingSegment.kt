package com.acme.transport.safety.coaching.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The coaching segment records of safety's coaching work, and the rules, stores, and calculations around
// them.

/** Where a coaching segment is in its life cycle. */
enum class CoachingSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A coaching segment as the coaching team keeps it. */
data class CoachingSegment(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var windowDays: Int,
    var maximum: Double,
    var threshold: Double,
    var loadKg: Double,
    var attempts: Int,
    var status: CoachingSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingSegmentStatus): Boolean {
        when (status) {
            CoachingSegmentStatus.IDLE -> return next == CoachingSegmentStatus.READY || next == CoachingSegmentStatus.STOPPED
            CoachingSegmentStatus.WARMING -> return next == CoachingSegmentStatus.READY
            CoachingSegmentStatus.READY -> return next == CoachingSegmentStatus.STOPPED
            CoachingSegmentStatus.DRAINING -> {
                when (next) {
                    CoachingSegmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CoachingSegmentStatus) {
        if (!canMoveTo(next)) {
            error("coaching segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"coaching segment"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedValue(): Double = round((threshold + loadKg) * 100) / 100

    fun snapshot(): CoachingSegment {
        return CoachingSegment(
            code = code,
            title = title,
            tags = tags,
            windowDays = windowDays,
            maximum = maximum,
            threshold = threshold,
            loadKg = loadKg,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a coaching segment in status idle. */
fun newCoachingSegment(code: String, title: String): CoachingSegment {
    return CoachingSegment(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        windowDays = 0,
        maximum = 0.0,
        threshold = 0.0,
        loadKg = 0.0,
        attempts = 0,
        status = CoachingSegmentStatus.IDLE,
    )
}
