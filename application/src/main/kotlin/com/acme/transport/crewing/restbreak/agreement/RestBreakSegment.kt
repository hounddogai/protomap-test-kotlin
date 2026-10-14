package com.acme.transport.crewing.restbreak.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rest break segment is in its life cycle. */
enum class RestBreakSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rest break segment as the rest break team keeps it. */
data class RestBreakSegment(
    var code: String,
    var label: String,
    var visible: Boolean,
    var revision: Int,
    var minimum: Double,
    var status: RestBreakSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakSegmentStatus): Boolean {
        when (status) {
            RestBreakSegmentStatus.IDLE -> {
                when (next) {
                    RestBreakSegmentStatus.READY, RestBreakSegmentStatus.STOPPED -> return true
                }
                return false
            }
            RestBreakSegmentStatus.WARMING -> {
                when (next) {
                    RestBreakSegmentStatus.DRAINING, RestBreakSegmentStatus.STOPPED -> return true
                }
                return false
            }
            RestBreakSegmentStatus.READY -> return next == RestBreakSegmentStatus.STOPPED
            RestBreakSegmentStatus.DRAINING -> return next == RestBreakSegmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RestBreakSegmentStatus) {
        if (!canMoveTo(next)) {
            error("rest break segment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"rest break segment"} $code"

    fun adjustedValue(): Double = round((revision.toDouble() + minimum) * 100) / 100

    fun snapshot(): RestBreakSegment {
        return RestBreakSegment(
            code = code,
            label = label,
            visible = visible,
            revision = revision,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rest break segment in status idle. */
fun newRestBreakSegment(code: String, label: String): RestBreakSegment {
    return RestBreakSegment(
        code = code,
        label = label,
        visible = false,
        revision = 0,
        minimum = 0.0,
        status = RestBreakSegmentStatus.IDLE,
    )
}
