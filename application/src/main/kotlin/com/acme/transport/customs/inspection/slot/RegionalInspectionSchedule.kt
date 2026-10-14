package com.acme.transport.customs.inspection.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional inspection schedule is in its life cycle. */
enum class RegionalInspectionScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One regional inspection schedule, with what the planners need to act on it. */
data class RegionalInspectionSchedule(
    var code: String,
    var name: String,
    var reviewer: String,
    var durationMinutes: Int,
    var floorValue: Double,
    var channel: String,
    var version: Int,
    var country: String,
    var status: RegionalInspectionScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalInspectionScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalInspectionScheduleStatus): Boolean {
        when (status) {
            RegionalInspectionScheduleStatus.NEW -> {
                when (next) {
                    RegionalInspectionScheduleStatus.QUEUED, RegionalInspectionScheduleStatus.DONE -> return true
                }
                return false
            }
            RegionalInspectionScheduleStatus.QUEUED -> {
                when (next) {
                    RegionalInspectionScheduleStatus.RUNNING -> return true
                }
                return false
            }
            RegionalInspectionScheduleStatus.RUNNING -> {
                when (next) {
                    RegionalInspectionScheduleStatus.DONE, RegionalInspectionScheduleStatus.FAILED -> return true
                }
                return false
            }
            RegionalInspectionScheduleStatus.DONE -> {
                when (next) {
                    RegionalInspectionScheduleStatus.FAILED -> return true
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
        version += 1
    }

    fun label(): String = "$code: $name"

    fun effectiveLoad(): Double = round((durationMinutes.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): RegionalInspectionSchedule {
        return RegionalInspectionSchedule(
            code = code,
            name = name,
            reviewer = reviewer,
            durationMinutes = durationMinutes,
            floorValue = floorValue,
            channel = channel,
            version = version,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional inspection schedule in status new. */
fun newRegionalInspectionSchedule(code: String, name: String): RegionalInspectionSchedule {
    return RegionalInspectionSchedule(
        code = code,
        name = name,
        reviewer = "",
        durationMinutes = 0,
        floorValue = 0.0,
        channel = "",
        version = 0,
        country = "",
        status = RegionalInspectionScheduleStatus.NEW,
    )
}
