package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a duty period segment is in its life cycle. */
enum class DutyPeriodSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One duty period segment, with what the planners need to act on it. */
data class DutyPeriodSegment(
    var code: String,
    var title: String,
    var score: Double,
    var lane: String,
    var stopCount: Int,
    var externalRef: String,
    var currency: String,
    var status: DutyPeriodSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodSegmentStatus): Boolean {
        when (status) {
            DutyPeriodSegmentStatus.PROPOSED -> {
                when (next) {
                    DutyPeriodSegmentStatus.APPROVED, DutyPeriodSegmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            DutyPeriodSegmentStatus.APPROVED -> return next == DutyPeriodSegmentStatus.REJECTED || next == DutyPeriodSegmentStatus.WITHDRAWN
            DutyPeriodSegmentStatus.REJECTED -> return next == DutyPeriodSegmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DutyPeriodSegmentStatus) {
        if (!canMoveTo(next)) {
            error("duty period segment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedExposure(): Double = round(stopCount.toDouble() * score * 100) / 100

    fun snapshot(): DutyPeriodSegment {
        return DutyPeriodSegment(
            code = code,
            title = title,
            score = score,
            lane = lane,
            stopCount = stopCount,
            externalRef = externalRef,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a duty period segment in status proposed. */
fun newDutyPeriodSegment(code: String, title: String): DutyPeriodSegment {
    return DutyPeriodSegment(
        code = code,
        title = title,
        score = 0.0,
        lane = "",
        stopCount = 0,
        externalRef = "",
        currency = "",
        status = DutyPeriodSegmentStatus.PROPOSED,
    )
}
