package com.acme.transport.crewing.roster.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a roster segment is in its life cycle. */
enum class RosterSegmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A roster segment. Its code is unique within a tenant. */
data class RosterSegment(
    var code: String,
    var label: String,
    var segment: String,
    var ceiling: Double,
    var title: String,
    var visible: Boolean,
    var lane: String,
    var locale: String,
    var depot: String,
    var discount: Double,
    var status: RosterSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterSegmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterSegmentStatus): Boolean {
        when (status) {
            RosterSegmentStatus.OPEN -> return next == RosterSegmentStatus.REOPENED
            RosterSegmentStatus.IN_REVIEW -> return next == RosterSegmentStatus.RESOLVED || next == RosterSegmentStatus.ARCHIVED
            RosterSegmentStatus.RESOLVED -> {
                when (next) {
                    RosterSegmentStatus.REOPENED, RosterSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
            RosterSegmentStatus.REOPENED -> {
                when (next) {
                    RosterSegmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterSegmentStatus) {
        if (!canMoveTo(next)) {
            error("roster segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"roster segment"} $code"

    fun projectedValue(): Double = round(ceiling * discount * 100) / 100

    fun snapshot(): RosterSegment {
        return RosterSegment(
            code = code,
            label = label,
            segment = segment,
            ceiling = ceiling,
            title = title,
            visible = visible,
            lane = lane,
            locale = locale,
            depot = depot,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a roster segment in status open. */
fun newRosterSegment(code: String, label: String): RosterSegment {
    return RosterSegment(
        code = code,
        label = label,
        segment = "",
        ceiling = 0.0,
        title = "",
        visible = false,
        lane = "",
        locale = "",
        depot = "",
        discount = 0.0,
        status = RosterSegmentStatus.OPEN,
    )
}
