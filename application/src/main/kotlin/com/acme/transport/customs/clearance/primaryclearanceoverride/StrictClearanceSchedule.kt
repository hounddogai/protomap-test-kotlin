package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict clearance schedule is in its life cycle. */
enum class StrictClearanceScheduleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict clearance schedule as the clearance team keeps it. */
data class StrictClearanceSchedule(
    var code: String,
    var label: String,
    var startsAt: Int,
    var loadKg: Double,
    var summary: String,
    var weight: Double,
    var routeCode: String,
    var name: String,
    var status: StrictClearanceScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictClearanceScheduleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictClearanceScheduleStatus): Boolean {
        when (status) {
            StrictClearanceScheduleStatus.NEW -> {
                when (next) {
                    StrictClearanceScheduleStatus.DONE -> return true
                }
                return false
            }
            StrictClearanceScheduleStatus.QUEUED -> {
                when (next) {
                    StrictClearanceScheduleStatus.RUNNING, StrictClearanceScheduleStatus.FAILED -> return true
                }
                return false
            }
            StrictClearanceScheduleStatus.RUNNING -> {
                when (next) {
                    StrictClearanceScheduleStatus.DONE, StrictClearanceScheduleStatus.FAILED -> return true
                }
                return false
            }
            StrictClearanceScheduleStatus.DONE -> {
                when (next) {
                    StrictClearanceScheduleStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictClearanceScheduleStatus) {
        if (!canMoveTo(next)) {
            error("strict clearance schedule $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"strict clearance schedule"} $code"

    fun adjustedExposure(): Double = round((loadKg - startsAt.toDouble()) * 100) / 100

    fun snapshot(): StrictClearanceSchedule {
        return StrictClearanceSchedule(
            code = code,
            label = label,
            startsAt = startsAt,
            loadKg = loadKg,
            summary = summary,
            weight = weight,
            routeCode = routeCode,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict clearance schedule in status new. */
fun newStrictClearanceSchedule(code: String, label: String): StrictClearanceSchedule {
    return StrictClearanceSchedule(
        code = code,
        label = label,
        startsAt = 0,
        loadKg = 0.0,
        summary = "",
        weight = 0.0,
        routeCode = "",
        name = "",
        status = StrictClearanceScheduleStatus.NEW,
    )
}
