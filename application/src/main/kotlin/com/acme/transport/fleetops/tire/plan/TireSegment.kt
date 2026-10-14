package com.acme.transport.fleetops.tire.plan

import com.acme.transport.kit.text.normalize

/** Where a tire segment is in its life cycle. */
enum class TireSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a tire segment and its current status. */
data class TireSegment(
    var code: String,
    var label: String,
    var name: String,
    var stopCount: Int,
    var depot: String,
    var status: TireSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireSegmentStatus): Boolean {
        when (status) {
            TireSegmentStatus.IDLE -> {
                when (next) {
                    TireSegmentStatus.DRAINING, TireSegmentStatus.STOPPED -> return true
                }
                return false
            }
            TireSegmentStatus.WARMING -> return next == TireSegmentStatus.READY || next == TireSegmentStatus.DRAINING
            TireSegmentStatus.READY -> return next == TireSegmentStatus.STOPPED
            TireSegmentStatus.DRAINING -> return next == TireSegmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TireSegmentStatus) {
        if (!canMoveTo(next)) {
            error("tire segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): TireSegment {
        return TireSegment(
            code = code,
            label = label,
            name = name,
            stopCount = stopCount,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a tire segment in status idle. */
fun newTireSegment(code: String, label: String): TireSegment {
    return TireSegment(
        code = code,
        label = label,
        name = "",
        stopCount = 0,
        depot = "",
        status = TireSegmentStatus.IDLE,
    )
}
