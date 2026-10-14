package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.text.normalize

// The scheduled inspection score records of fleetops's inspection work, and the rules, stores, and
// calculations around them.

/** Where a scheduled inspection score is in its life cycle. */
enum class ScheduledInspectionScoreStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One scheduled inspection score, with what the planners need to act on it. */
data class ScheduledInspectionScore(
    var code: String,
    var name: String,
    var checksum: String,
    var lane: String,
    var cost: Double,
    var status: ScheduledInspectionScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledInspectionScoreStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledInspectionScoreStatus): Boolean {
        when (status) {
            ScheduledInspectionScoreStatus.IDLE -> {
                when (next) {
                    ScheduledInspectionScoreStatus.READY -> return true
                }
                return false
            }
            ScheduledInspectionScoreStatus.WARMING -> return next == ScheduledInspectionScoreStatus.READY
            ScheduledInspectionScoreStatus.READY -> return next == ScheduledInspectionScoreStatus.DRAINING || next == ScheduledInspectionScoreStatus.STOPPED
            ScheduledInspectionScoreStatus.DRAINING -> return next == ScheduledInspectionScoreStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledInspectionScoreStatus) {
        if (!canMoveTo(next)) {
            error("scheduled inspection score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled inspection score"} $code"

    fun measuredValue(): Double = cost

    fun snapshot(): ScheduledInspectionScore {
        return ScheduledInspectionScore(
            code = code,
            name = name,
            checksum = checksum,
            lane = lane,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled inspection score in status idle. */
fun newScheduledInspectionScore(code: String, name: String): ScheduledInspectionScore {
    return ScheduledInspectionScore(
        code = code,
        name = name,
        checksum = "",
        lane = "",
        cost = 0.0,
        status = ScheduledInspectionScoreStatus.IDLE,
    )
}
