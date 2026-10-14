package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped attempt segment is in its life cycle. */
enum class CappedAttemptSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A capped attempt segment as the attempt team keeps it. */
data class CappedAttemptSegment(
    var code: String,
    var title: String,
    var minimum: Double,
    var depot: String,
    var margin: Double,
    var ceiling: Double,
    var stopCount: Int,
    var status: CappedAttemptSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAttemptSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAttemptSegmentStatus): Boolean {
        when (status) {
            CappedAttemptSegmentStatus.IDLE -> {
                when (next) {
                    CappedAttemptSegmentStatus.READY, CappedAttemptSegmentStatus.STOPPED -> return true
                }
                return false
            }
            CappedAttemptSegmentStatus.WARMING -> {
                when (next) {
                    CappedAttemptSegmentStatus.READY -> return true
                }
                return false
            }
            CappedAttemptSegmentStatus.READY -> return next == CappedAttemptSegmentStatus.DRAINING
            CappedAttemptSegmentStatus.DRAINING -> {
                when (next) {
                    CappedAttemptSegmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedAttemptSegmentStatus) {
        if (!canMoveTo(next)) {
            error("capped attempt segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedTotal(): Double = round((margin + minimum) * 100) / 100

    fun snapshot(): CappedAttemptSegment {
        return CappedAttemptSegment(
            code = code,
            title = title,
            minimum = minimum,
            depot = depot,
            margin = margin,
            ceiling = ceiling,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a capped attempt segment in status idle. */
fun newCappedAttemptSegment(code: String, title: String): CappedAttemptSegment {
    return CappedAttemptSegment(
        code = code,
        title = title,
        minimum = 0.0,
        depot = "",
        margin = 0.0,
        ceiling = 0.0,
        stopCount = 0,
        status = CappedAttemptSegmentStatus.IDLE,
    )
}
