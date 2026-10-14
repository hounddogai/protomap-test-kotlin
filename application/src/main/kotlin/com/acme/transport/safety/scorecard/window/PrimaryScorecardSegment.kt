package com.acme.transport.safety.scorecard.window

import com.acme.transport.kit.text.normalize

/** Where a primary scorecard segment is in its life cycle. */
enum class PrimaryScorecardSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary scorecard segment. Its code is unique within a tenant. */
data class PrimaryScorecardSegment(
    var code: String,
    var name: String,
    var title: String,
    var reviewer: String,
    var windowDays: Int,
    var carrier: String,
    var status: PrimaryScorecardSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryScorecardSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryScorecardSegmentStatus): Boolean {
        when (status) {
            PrimaryScorecardSegmentStatus.IDLE -> {
                when (next) {
                    PrimaryScorecardSegmentStatus.STOPPED -> return true
                }
                return false
            }
            PrimaryScorecardSegmentStatus.WARMING -> {
                when (next) {
                    PrimaryScorecardSegmentStatus.READY -> return true
                }
                return false
            }
            PrimaryScorecardSegmentStatus.READY -> return next == PrimaryScorecardSegmentStatus.STOPPED
            PrimaryScorecardSegmentStatus.DRAINING -> {
                when (next) {
                    PrimaryScorecardSegmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryScorecardSegmentStatus) {
        if (!canMoveTo(next)) {
            error("primary scorecard segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary scorecard segment"} $code"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): PrimaryScorecardSegment {
        return PrimaryScorecardSegment(
            code = code,
            name = name,
            title = title,
            reviewer = reviewer,
            windowDays = windowDays,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a primary scorecard segment in status idle. */
fun newPrimaryScorecardSegment(code: String, name: String): PrimaryScorecardSegment {
    return PrimaryScorecardSegment(
        code = code,
        name = name,
        title = "",
        reviewer = "",
        windowDays = 0,
        carrier = "",
        status = PrimaryScorecardSegmentStatus.IDLE,
    )
}
