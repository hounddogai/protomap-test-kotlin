package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.kit.text.normalize

/** Where a tire schedule is in its life cycle. */
enum class TireScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One tire schedule, with what the planners need to act on it. */
data class TireSchedule(
    var code: String,
    var label: String,
    var region: String,
    var category: String,
    var loadKg: Double,
    var status: TireScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireScheduleStatus): Boolean {
        when (status) {
            TireScheduleStatus.IDLE -> {
                when (next) {
                    TireScheduleStatus.STOPPED -> return true
                }
                return false
            }
            TireScheduleStatus.WARMING -> return next == TireScheduleStatus.STOPPED
            TireScheduleStatus.READY -> {
                when (next) {
                    TireScheduleStatus.DRAINING, TireScheduleStatus.STOPPED -> return true
                }
                return false
            }
            TireScheduleStatus.DRAINING -> return next == TireScheduleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: TireScheduleStatus) {
        if (!canMoveTo(next)) {
            error("tire schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = loadKg

    fun snapshot(): TireSchedule {
        return TireSchedule(
            code = code,
            label = label,
            region = region,
            category = category,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a tire schedule in status idle. */
fun newTireSchedule(code: String, label: String): TireSchedule {
    return TireSchedule(
        code = code,
        label = label,
        region = "",
        category = "",
        loadKg = 0.0,
        status = TireScheduleStatus.IDLE,
    )
}
