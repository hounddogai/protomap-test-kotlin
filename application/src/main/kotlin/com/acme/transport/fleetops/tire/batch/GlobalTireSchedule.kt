package com.acme.transport.fleetops.tire.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global tire schedule is in its life cycle. */
enum class GlobalTireScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global tire schedule as the tire team keeps it. */
data class GlobalTireSchedule(
    var code: String,
    var name: String,
    var notes: String,
    var unit: String,
    var amount: Double,
    var summary: String,
    var timezone: String,
    var label: String,
    var weight: Double,
    var status: GlobalTireScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTireScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTireScheduleStatus): Boolean {
        when (status) {
            GlobalTireScheduleStatus.IDLE -> return next == GlobalTireScheduleStatus.READY
            GlobalTireScheduleStatus.WARMING -> {
                when (next) {
                    GlobalTireScheduleStatus.READY -> return true
                }
                return false
            }
            GlobalTireScheduleStatus.READY -> {
                when (next) {
                    GlobalTireScheduleStatus.DRAINING -> return true
                }
                return false
            }
            GlobalTireScheduleStatus.DRAINING -> return next == GlobalTireScheduleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: GlobalTireScheduleStatus) {
        if (!canMoveTo(next)) {
            error("global tire schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($name)"

    fun adjustedTotal(): Double = round((amount + weight) * 100) / 100

    fun snapshot(): GlobalTireSchedule {
        return GlobalTireSchedule(
            code = code,
            name = name,
            notes = notes,
            unit = unit,
            amount = amount,
            summary = summary,
            timezone = timezone,
            label = label,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a global tire schedule in status idle. */
fun newGlobalTireSchedule(code: String, name: String): GlobalTireSchedule {
    return GlobalTireSchedule(
        code = code,
        name = name,
        notes = "",
        unit = "",
        amount = 0.0,
        summary = "",
        timezone = "",
        label = "",
        weight = 0.0,
        status = GlobalTireScheduleStatus.IDLE,
    )
}
