package com.acme.transport.crewing.swap.schedule

import com.acme.transport.kit.text.normalize

// The swap schedule records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a swap schedule is in its life cycle. */
enum class SwapScheduleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a swap schedule and its current status. */
data class SwapSchedule(
    var code: String,
    var name: String,
    var odometer: Int,
    var archived: Boolean,
    var active: Boolean,
    var trailerId: String,
    var status: SwapScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapScheduleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapScheduleStatus): Boolean {
        when (status) {
            SwapScheduleStatus.OPEN -> return next == SwapScheduleStatus.RESOLVED
            SwapScheduleStatus.IN_REVIEW -> return next == SwapScheduleStatus.ARCHIVED
            SwapScheduleStatus.RESOLVED -> return next == SwapScheduleStatus.REOPENED
            SwapScheduleStatus.REOPENED -> return next == SwapScheduleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SwapScheduleStatus) {
        if (!canMoveTo(next)) {
            error("swap schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): SwapSchedule {
        return SwapSchedule(
            code = code,
            name = name,
            odometer = odometer,
            archived = archived,
            active = active,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a swap schedule in status open. */
fun newSwapSchedule(code: String, name: String): SwapSchedule {
    return SwapSchedule(
        code = code,
        name = name,
        odometer = 0,
        archived = false,
        active = false,
        trailerId = "",
        status = SwapScheduleStatus.OPEN,
    )
}
