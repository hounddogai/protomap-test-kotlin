package com.acme.transport.fleetops.inspection.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional inspection schedule is in its life cycle. */
enum class RegionalInspectionScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One regional inspection schedule, with what the planners need to act on it. */
data class RegionalInspectionSchedule(
    var code: String,
    var label: String,
    var priority: Int,
    var trailerId: String,
    var quantity: Int,
    var status: RegionalInspectionScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalInspectionScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalInspectionScheduleStatus): Boolean {
        when (status) {
            RegionalInspectionScheduleStatus.OPEN -> return next == RegionalInspectionScheduleStatus.RESOLVED || next == RegionalInspectionScheduleStatus.REOPENED
            RegionalInspectionScheduleStatus.IN_REVIEW -> {
                when (next) {
                    RegionalInspectionScheduleStatus.RESOLVED -> return true
                }
                return false
            }
            RegionalInspectionScheduleStatus.RESOLVED -> {
                when (next) {
                    RegionalInspectionScheduleStatus.REOPENED -> return true
                }
                return false
            }
            RegionalInspectionScheduleStatus.REOPENED -> {
                when (next) {
                    RegionalInspectionScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalInspectionScheduleStatus) {
        if (!canMoveTo(next)) {
            error("regional inspection schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netVolume(): Double = round((priority.toDouble() + quantity.toDouble()) * 100) / 100

    fun snapshot(): RegionalInspectionSchedule {
        return RegionalInspectionSchedule(
            code = code,
            label = label,
            priority = priority,
            trailerId = trailerId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional inspection schedule in status open. */
fun newRegionalInspectionSchedule(code: String, label: String): RegionalInspectionSchedule {
    return RegionalInspectionSchedule(
        code = code,
        label = label,
        priority = 0,
        trailerId = "",
        quantity = 0,
        status = RegionalInspectionScheduleStatus.OPEN,
    )
}
