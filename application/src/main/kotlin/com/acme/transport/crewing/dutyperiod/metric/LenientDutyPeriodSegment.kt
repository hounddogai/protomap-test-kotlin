package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.kit.text.normalize

/** Where a lenient duty period segment is in its life cycle. */
enum class LenientDutyPeriodSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A lenient duty period segment as the duty period team keeps it. */
data class LenientDutyPeriodSegment(
    var code: String,
    var label: String,
    var notes: String,
    var expiresAt: Int,
    var ownerId: String,
    var status: LenientDutyPeriodSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDutyPeriodSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDutyPeriodSegmentStatus): Boolean {
        when (status) {
            LenientDutyPeriodSegmentStatus.PLANNED -> return next == LenientDutyPeriodSegmentStatus.SCHEDULED
            LenientDutyPeriodSegmentStatus.SCHEDULED -> return next == LenientDutyPeriodSegmentStatus.IN_PROGRESS
            LenientDutyPeriodSegmentStatus.IN_PROGRESS -> {
                when (next) {
                    LenientDutyPeriodSegmentStatus.COMPLETED -> return true
                }
                return false
            }
            LenientDutyPeriodSegmentStatus.COMPLETED -> return next == LenientDutyPeriodSegmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LenientDutyPeriodSegmentStatus) {
        if (!canMoveTo(next)) {
            error("lenient duty period segment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): LenientDutyPeriodSegment {
        return LenientDutyPeriodSegment(
            code = code,
            label = label,
            notes = notes,
            expiresAt = expiresAt,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient duty period segment in status planned. */
fun newLenientDutyPeriodSegment(code: String, label: String): LenientDutyPeriodSegment {
    return LenientDutyPeriodSegment(
        code = code,
        label = label,
        notes = "",
        expiresAt = 0,
        ownerId = "",
        status = LenientDutyPeriodSegmentStatus.PLANNED,
    )
}
