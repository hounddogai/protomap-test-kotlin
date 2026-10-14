package com.acme.transport.safety.scorecard.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scorecard schedule records of safety's scorecard work, and the rules, stores, and calculations around
// them.

/** Where a scorecard schedule is in its life cycle. */
enum class ScorecardScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A scorecard schedule as the scorecard team keeps it. */
data class ScorecardSchedule(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var windowDays: Int,
    var title: String,
    var vehicleId: String,
    var trailerId: String,
    var limit: Int,
    var quantity: Int,
    var country: String,
    var status: ScorecardScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardScheduleStatus): Boolean {
        when (status) {
            ScorecardScheduleStatus.IDLE -> return next == ScorecardScheduleStatus.WARMING || next == ScorecardScheduleStatus.READY
            ScorecardScheduleStatus.WARMING -> {
                when (next) {
                    ScorecardScheduleStatus.DRAINING, ScorecardScheduleStatus.STOPPED -> return true
                }
                return false
            }
            ScorecardScheduleStatus.READY -> {
                when (next) {
                    ScorecardScheduleStatus.DRAINING, ScorecardScheduleStatus.STOPPED -> return true
                }
                return false
            }
            ScorecardScheduleStatus.DRAINING -> {
                when (next) {
                    ScorecardScheduleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardScheduleStatus) {
        if (!canMoveTo(next)) {
            error("scorecard schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveValue(): Double = round(limit.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): ScorecardSchedule {
        return ScorecardSchedule(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            windowDays = windowDays,
            title = title,
            vehicleId = vehicleId,
            trailerId = trailerId,
            limit = limit,
            quantity = quantity,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scorecard schedule in status idle. */
fun newScorecardSchedule(code: String, name: String): ScorecardSchedule {
    return ScorecardSchedule(
        code = code,
        name = name,
        etaMinutes = 0,
        windowDays = 0,
        title = "",
        vehicleId = "",
        trailerId = "",
        limit = 0,
        quantity = 0,
        country = "",
        status = ScorecardScheduleStatus.IDLE,
    )
}
