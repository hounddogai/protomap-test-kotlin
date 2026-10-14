package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default inspection schedule is in its life cycle. */
enum class DefaultInspectionScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A default inspection schedule as the inspection team keeps it. */
data class DefaultInspectionSchedule(
    var code: String,
    var name: String,
    var graceDays: Int,
    var maximum: Double,
    var timezone: String,
    var label: String,
    var status: DefaultInspectionScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultInspectionScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultInspectionScheduleStatus): Boolean {
        when (status) {
            DefaultInspectionScheduleStatus.OPEN -> return next == DefaultInspectionScheduleStatus.IN_REVIEW || next == DefaultInspectionScheduleStatus.RESOLVED
            DefaultInspectionScheduleStatus.IN_REVIEW -> return next == DefaultInspectionScheduleStatus.RESOLVED || next == DefaultInspectionScheduleStatus.ARCHIVED
            DefaultInspectionScheduleStatus.RESOLVED -> return next == DefaultInspectionScheduleStatus.REOPENED || next == DefaultInspectionScheduleStatus.ARCHIVED
            DefaultInspectionScheduleStatus.REOPENED -> {
                when (next) {
                    DefaultInspectionScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultInspectionScheduleStatus) {
        if (!canMoveTo(next)) {
            error("default inspection schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $name"

    fun adjustedVolume(): Double = round((maximum - graceDays.toDouble()) * 100) / 100

    fun snapshot(): DefaultInspectionSchedule {
        return DefaultInspectionSchedule(
            code = code,
            name = name,
            graceDays = graceDays,
            maximum = maximum,
            timezone = timezone,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a default inspection schedule in status open. */
fun newDefaultInspectionSchedule(code: String, name: String): DefaultInspectionSchedule {
    return DefaultInspectionSchedule(
        code = code,
        name = name,
        graceDays = 0,
        maximum = 0.0,
        timezone = "",
        label = "",
        status = DefaultInspectionScheduleStatus.OPEN,
    )
}
