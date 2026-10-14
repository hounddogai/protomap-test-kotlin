package com.acme.transport.safety.coaching.estimate

import com.acme.transport.kit.text.normalize

/** Where a global coaching schedule is in its life cycle. */
enum class GlobalCoachingScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A global coaching schedule. Its code is unique within a tenant. */
data class GlobalCoachingSchedule(
    var code: String,
    var label: String,
    var archived: Boolean,
    var capacity: Int,
    var segment: String,
    var status: GlobalCoachingScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCoachingScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCoachingScheduleStatus): Boolean {
        when (status) {
            GlobalCoachingScheduleStatus.OPEN -> {
                when (next) {
                    GlobalCoachingScheduleStatus.RESOLVED, GlobalCoachingScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            GlobalCoachingScheduleStatus.IN_REVIEW -> return next == GlobalCoachingScheduleStatus.RESOLVED || next == GlobalCoachingScheduleStatus.ARCHIVED
            GlobalCoachingScheduleStatus.RESOLVED -> return next == GlobalCoachingScheduleStatus.REOPENED || next == GlobalCoachingScheduleStatus.ARCHIVED
            GlobalCoachingScheduleStatus.REOPENED -> {
                when (next) {
                    GlobalCoachingScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCoachingScheduleStatus) {
        if (!canMoveTo(next)) {
            error("global coaching schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"global coaching schedule"} $code"

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): GlobalCoachingSchedule {
        return GlobalCoachingSchedule(
            code = code,
            label = label,
            archived = archived,
            capacity = capacity,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a global coaching schedule in status open. */
fun newGlobalCoachingSchedule(code: String, label: String): GlobalCoachingSchedule {
    return GlobalCoachingSchedule(
        code = code,
        label = label,
        archived = false,
        capacity = 0,
        segment = "",
        status = GlobalCoachingScheduleStatus.OPEN,
    )
}
