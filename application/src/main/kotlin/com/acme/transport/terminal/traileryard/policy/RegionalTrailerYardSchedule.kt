package com.acme.transport.terminal.traileryard.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional trailer yard schedule is in its life cycle. */
enum class RegionalTrailerYardScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One regional trailer yard schedule, with what the planners need to act on it. */
data class RegionalTrailerYardSchedule(
    var code: String,
    var label: String,
    var windowDays: Int,
    var rate: Double,
    var weight: Double,
    var approved: Boolean,
    var ownerId: String,
    var limit: Int,
    var status: RegionalTrailerYardScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalTrailerYardScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalTrailerYardScheduleStatus): Boolean {
        when (status) {
            RegionalTrailerYardScheduleStatus.OPEN -> return next == RegionalTrailerYardScheduleStatus.REOPENED || next == RegionalTrailerYardScheduleStatus.ARCHIVED
            RegionalTrailerYardScheduleStatus.IN_REVIEW -> return next == RegionalTrailerYardScheduleStatus.ARCHIVED
            RegionalTrailerYardScheduleStatus.RESOLVED -> {
                when (next) {
                    RegionalTrailerYardScheduleStatus.REOPENED, RegionalTrailerYardScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalTrailerYardScheduleStatus.REOPENED -> {
                when (next) {
                    RegionalTrailerYardScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalTrailerYardScheduleStatus) {
        if (!canMoveTo(next)) {
            error("regional trailer yard schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveTotal(): Double = round(weight * windowDays.toDouble() * 100) / 100

    fun snapshot(): RegionalTrailerYardSchedule {
        return RegionalTrailerYardSchedule(
            code = code,
            label = label,
            windowDays = windowDays,
            rate = rate,
            weight = weight,
            approved = approved,
            ownerId = ownerId,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional trailer yard schedule in status open. */
fun newRegionalTrailerYardSchedule(code: String, label: String): RegionalTrailerYardSchedule {
    return RegionalTrailerYardSchedule(
        code = code,
        label = label,
        windowDays = 0,
        rate = 0.0,
        weight = 0.0,
        approved = false,
        ownerId = "",
        limit = 0,
        status = RegionalTrailerYardScheduleStatus.OPEN,
    )
}
