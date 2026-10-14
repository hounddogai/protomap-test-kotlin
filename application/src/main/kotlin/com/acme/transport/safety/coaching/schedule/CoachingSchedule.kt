package com.acme.transport.safety.coaching.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The coaching schedule records of safety's coaching work, and the rules, stores, and calculations around
// them.

/** Where a coaching schedule is in its life cycle. */
enum class CoachingScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a coaching schedule and its current status. */
data class CoachingSchedule(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var phone: String,
    var pinned: Boolean,
    var priority: Int,
    var limit: Int,
    var startsAt: Int,
    var carrier: String,
    var status: CoachingScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingScheduleStatus): Boolean {
        when (status) {
            CoachingScheduleStatus.NEW -> {
                when (next) {
                    CoachingScheduleStatus.QUEUED, CoachingScheduleStatus.DONE -> return true
                }
                return false
            }
            CoachingScheduleStatus.QUEUED -> {
                when (next) {
                    CoachingScheduleStatus.RUNNING, CoachingScheduleStatus.DONE -> return true
                }
                return false
            }
            CoachingScheduleStatus.RUNNING -> {
                when (next) {
                    CoachingScheduleStatus.DONE, CoachingScheduleStatus.FAILED -> return true
                }
                return false
            }
            CoachingScheduleStatus.DONE -> return next == CoachingScheduleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CoachingScheduleStatus) {
        if (!canMoveTo(next)) {
            error("coaching schedule $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedExposure(): Double = round((expiresAt.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): CoachingSchedule {
        return CoachingSchedule(
            code = code,
            title = title,
            expiresAt = expiresAt,
            phone = phone,
            pinned = pinned,
            priority = priority,
            limit = limit,
            startsAt = startsAt,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a coaching schedule in status new. */
fun newCoachingSchedule(code: String, title: String): CoachingSchedule {
    return CoachingSchedule(
        code = code,
        title = title,
        expiresAt = 0,
        phone = "",
        pinned = false,
        priority = 0,
        limit = 0,
        startsAt = 0,
        carrier = "",
        status = CoachingScheduleStatus.NEW,
    )
}
