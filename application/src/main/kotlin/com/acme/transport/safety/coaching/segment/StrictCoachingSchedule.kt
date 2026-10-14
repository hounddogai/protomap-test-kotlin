package com.acme.transport.safety.coaching.segment

import com.acme.transport.kit.text.normalize

/** Where a strict coaching schedule is in its life cycle. */
enum class StrictCoachingScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One strict coaching schedule, with what the planners need to act on it. */
data class StrictCoachingSchedule(
    var code: String,
    var title: String,
    var channel: String,
    var externalRef: String,
    var ceiling: Double,
    var status: StrictCoachingScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCoachingScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCoachingScheduleStatus): Boolean {
        when (status) {
            StrictCoachingScheduleStatus.NEW -> {
                when (next) {
                    StrictCoachingScheduleStatus.FAILED -> return true
                }
                return false
            }
            StrictCoachingScheduleStatus.QUEUED -> return next == StrictCoachingScheduleStatus.RUNNING || next == StrictCoachingScheduleStatus.FAILED
            StrictCoachingScheduleStatus.RUNNING -> {
                when (next) {
                    StrictCoachingScheduleStatus.DONE -> return true
                }
                return false
            }
            StrictCoachingScheduleStatus.DONE -> return next == StrictCoachingScheduleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictCoachingScheduleStatus) {
        if (!canMoveTo(next)) {
            error("strict coaching schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = ceiling

    fun snapshot(): StrictCoachingSchedule {
        return StrictCoachingSchedule(
            code = code,
            title = title,
            channel = channel,
            externalRef = externalRef,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a strict coaching schedule in status new. */
fun newStrictCoachingSchedule(code: String, title: String): StrictCoachingSchedule {
    return StrictCoachingSchedule(
        code = code,
        title = title,
        channel = "",
        externalRef = "",
        ceiling = 0.0,
        status = StrictCoachingScheduleStatus.NEW,
    )
}
