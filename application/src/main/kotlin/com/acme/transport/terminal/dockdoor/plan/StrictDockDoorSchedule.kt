package com.acme.transport.terminal.dockdoor.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict dock door schedule is in its life cycle. */
enum class StrictDockDoorScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict dock door schedule as the dock door team keeps it. */
data class StrictDockDoorSchedule(
    var code: String,
    var name: String,
    var margin: Double,
    var notes: String,
    var floorValue: Double,
    var amount: Double,
    var createdAt: Int,
    var phone: String,
    var status: StrictDockDoorScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDockDoorScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDockDoorScheduleStatus): Boolean {
        when (status) {
            StrictDockDoorScheduleStatus.OPEN -> return next == StrictDockDoorScheduleStatus.IN_REVIEW || next == StrictDockDoorScheduleStatus.REOPENED
            StrictDockDoorScheduleStatus.IN_REVIEW -> return next == StrictDockDoorScheduleStatus.RESOLVED || next == StrictDockDoorScheduleStatus.REOPENED
            StrictDockDoorScheduleStatus.RESOLVED -> {
                when (next) {
                    StrictDockDoorScheduleStatus.REOPENED, StrictDockDoorScheduleStatus.ARCHIVED -> return true
                }
                return false
            }
            StrictDockDoorScheduleStatus.REOPENED -> return next == StrictDockDoorScheduleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictDockDoorScheduleStatus) {
        if (!canMoveTo(next)) {
            error("strict dock door schedule $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round((floorValue - createdAt.toDouble()) * 100) / 100

    fun snapshot(): StrictDockDoorSchedule {
        return StrictDockDoorSchedule(
            code = code,
            name = name,
            margin = margin,
            notes = notes,
            floorValue = floorValue,
            amount = amount,
            createdAt = createdAt,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict dock door schedule in status open. */
fun newStrictDockDoorSchedule(code: String, name: String): StrictDockDoorSchedule {
    return StrictDockDoorSchedule(
        code = code,
        name = name,
        margin = 0.0,
        notes = "",
        floorValue = 0.0,
        amount = 0.0,
        createdAt = 0,
        phone = "",
        status = StrictDockDoorScheduleStatus.OPEN,
    )
}
