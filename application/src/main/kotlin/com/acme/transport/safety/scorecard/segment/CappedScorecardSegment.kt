package com.acme.transport.safety.scorecard.segment

import com.acme.transport.kit.text.normalize

/** Where a capped scorecard segment is in its life cycle. */
enum class CappedScorecardSegmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a capped scorecard segment and its current status. */
data class CappedScorecardSegment(
    var code: String,
    var name: String,
    var active: Boolean,
    var priority: Int,
    var title: String,
    var source: String,
    var depot: String,
    var region: String,
    var externalRef: String,
    var status: CappedScorecardSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedScorecardSegmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedScorecardSegmentStatus): Boolean {
        when (status) {
            CappedScorecardSegmentStatus.IDLE -> return next == CappedScorecardSegmentStatus.READY
            CappedScorecardSegmentStatus.WARMING -> {
                when (next) {
                    CappedScorecardSegmentStatus.READY, CappedScorecardSegmentStatus.DRAINING -> return true
                }
                return false
            }
            CappedScorecardSegmentStatus.READY -> return next == CappedScorecardSegmentStatus.STOPPED
            CappedScorecardSegmentStatus.DRAINING -> return next == CappedScorecardSegmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CappedScorecardSegmentStatus) {
        if (!canMoveTo(next)) {
            error("capped scorecard segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): CappedScorecardSegment {
        return CappedScorecardSegment(
            code = code,
            name = name,
            active = active,
            priority = priority,
            title = title,
            source = source,
            depot = depot,
            region = region,
            externalRef = externalRef,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a capped scorecard segment in status idle. */
fun newCappedScorecardSegment(code: String, name: String): CappedScorecardSegment {
    return CappedScorecardSegment(
        code = code,
        name = name,
        active = false,
        priority = 0,
        title = "",
        source = "",
        depot = "",
        region = "",
        externalRef = "",
        status = CappedScorecardSegmentStatus.IDLE,
    )
}
