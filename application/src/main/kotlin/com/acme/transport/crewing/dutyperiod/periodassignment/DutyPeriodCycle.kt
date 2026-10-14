package com.acme.transport.crewing.dutyperiod.periodassignment

import com.acme.transport.kit.text.normalize

/** Where a duty period cycle is in its life cycle. */
enum class DutyPeriodCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One duty period cycle, with what the planners need to act on it. */
data class DutyPeriodCycle(
    var code: String,
    var title: String,
    var active: Boolean,
    var unit: String,
    var driverId: String,
    var quantity: Int,
    var status: DutyPeriodCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodCycleStatus): Boolean {
        when (status) {
            DutyPeriodCycleStatus.IDLE -> {
                when (next) {
                    DutyPeriodCycleStatus.READY -> return true
                }
                return false
            }
            DutyPeriodCycleStatus.WARMING -> return next == DutyPeriodCycleStatus.READY || next == DutyPeriodCycleStatus.DRAINING
            DutyPeriodCycleStatus.READY -> {
                when (next) {
                    DutyPeriodCycleStatus.DRAINING, DutyPeriodCycleStatus.STOPPED -> return true
                }
                return false
            }
            DutyPeriodCycleStatus.DRAINING -> {
                when (next) {
                    DutyPeriodCycleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DutyPeriodCycleStatus) {
        if (!canMoveTo(next)) {
            error("duty period cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): DutyPeriodCycle {
        return DutyPeriodCycle(
            code = code,
            title = title,
            active = active,
            unit = unit,
            driverId = driverId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a duty period cycle in status idle. */
fun newDutyPeriodCycle(code: String, title: String): DutyPeriodCycle {
    return DutyPeriodCycle(
        code = code,
        title = title,
        active = false,
        unit = "",
        driverId = "",
        quantity = 0,
        status = DutyPeriodCycleStatus.IDLE,
    )
}
