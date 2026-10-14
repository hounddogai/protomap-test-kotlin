package com.acme.transport.crewing.restbreak.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global rest break segment records of crewing's rest break work, and the rules, stores, and calculations
// around them.

/** Where a global rest break segment is in its life cycle. */
enum class GlobalRestBreakSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One global rest break segment, with what the planners need to act on it. */
data class GlobalRestBreakSegment(
    var code: String,
    var name: String,
    var loadKg: Double,
    var limit: Int,
    var stopCount: Int,
    var status: GlobalRestBreakSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalRestBreakSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalRestBreakSegmentStatus): Boolean {
        when (status) {
            GlobalRestBreakSegmentStatus.PROPOSED -> return next == GlobalRestBreakSegmentStatus.WITHDRAWN
            GlobalRestBreakSegmentStatus.APPROVED -> return next == GlobalRestBreakSegmentStatus.REJECTED || next == GlobalRestBreakSegmentStatus.WITHDRAWN
            GlobalRestBreakSegmentStatus.REJECTED -> return next == GlobalRestBreakSegmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: GlobalRestBreakSegmentStatus) {
        if (!canMoveTo(next)) {
            error("global rest break segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveTotal(): Double = round(limit.toDouble() * loadKg * 100) / 100

    fun snapshot(): GlobalRestBreakSegment {
        return GlobalRestBreakSegment(
            code = code,
            name = name,
            loadKg = loadKg,
            limit = limit,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global rest break segment in status proposed. */
fun newGlobalRestBreakSegment(code: String, name: String): GlobalRestBreakSegment {
    return GlobalRestBreakSegment(
        code = code,
        name = name,
        loadKg = 0.0,
        limit = 0,
        stopCount = 0,
        status = GlobalRestBreakSegmentStatus.PROPOSED,
    )
}
