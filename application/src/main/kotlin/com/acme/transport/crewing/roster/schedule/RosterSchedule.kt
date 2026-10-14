package com.acme.transport.crewing.roster.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The roster schedule records of crewing's roster work, and the rules, stores, and calculations around them.

/** Where a roster schedule is in its life cycle. */
enum class RosterScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One roster schedule, with what the planners need to act on it. */
data class RosterSchedule(
    var code: String,
    var label: String,
    var priority: Int,
    var stopCount: Int,
    var locale: String,
    var minimum: Double,
    var distanceKm: Double,
    var status: RosterScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterScheduleStatus): Boolean {
        when (status) {
            RosterScheduleStatus.IDLE -> return next == RosterScheduleStatus.DRAINING || next == RosterScheduleStatus.STOPPED
            RosterScheduleStatus.WARMING -> {
                when (next) {
                    RosterScheduleStatus.READY, RosterScheduleStatus.STOPPED -> return true
                }
                return false
            }
            RosterScheduleStatus.READY -> return next == RosterScheduleStatus.DRAINING
            RosterScheduleStatus.DRAINING -> {
                when (next) {
                    RosterScheduleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterScheduleStatus) {
        if (!canMoveTo(next)) {
            error("roster schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"roster schedule"} $code"

    fun adjustedValue(): Double = round((distanceKm + minimum) * 100) / 100

    fun snapshot(): RosterSchedule {
        return RosterSchedule(
            code = code,
            label = label,
            priority = priority,
            stopCount = stopCount,
            locale = locale,
            minimum = minimum,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a roster schedule in status idle. */
fun newRosterSchedule(code: String, label: String): RosterSchedule {
    return RosterSchedule(
        code = code,
        label = label,
        priority = 0,
        stopCount = 0,
        locale = "",
        minimum = 0.0,
        distanceKm = 0.0,
        status = RosterScheduleStatus.IDLE,
    )
}
