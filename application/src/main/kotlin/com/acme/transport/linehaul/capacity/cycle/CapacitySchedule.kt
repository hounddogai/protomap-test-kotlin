package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity schedule is in its life cycle. */
enum class CapacityScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a capacity schedule and its current status. */
data class CapacitySchedule(
    var code: String,
    var name: String,
    var ceiling: Double,
    var unit: String,
    var etaMinutes: Int,
    var status: CapacityScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityScheduleStatus): Boolean {
        when (status) {
            CapacityScheduleStatus.IDLE -> {
                when (next) {
                    CapacityScheduleStatus.DRAINING, CapacityScheduleStatus.STOPPED -> return true
                }
                return false
            }
            CapacityScheduleStatus.WARMING -> {
                when (next) {
                    CapacityScheduleStatus.DRAINING, CapacityScheduleStatus.STOPPED -> return true
                }
                return false
            }
            CapacityScheduleStatus.READY -> return next == CapacityScheduleStatus.DRAINING || next == CapacityScheduleStatus.STOPPED
            CapacityScheduleStatus.DRAINING -> {
                when (next) {
                    CapacityScheduleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacityScheduleStatus) {
        if (!canMoveTo(next)) {
            error("capacity schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun effectiveValue(): Double = round((ceiling + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): CapacitySchedule {
        return CapacitySchedule(
            code = code,
            name = name,
            ceiling = ceiling,
            unit = unit,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a capacity schedule in status idle. */
fun newCapacitySchedule(code: String, name: String): CapacitySchedule {
    return CapacitySchedule(
        code = code,
        name = name,
        ceiling = 0.0,
        unit = "",
        etaMinutes = 0,
        status = CapacityScheduleStatus.IDLE,
    )
}
