package com.acme.transport.crewing.restbreak.breakrollup

import com.acme.transport.kit.text.normalize

/** Where a scheduled rest break plan is in its life cycle. */
enum class ScheduledRestBreakPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A scheduled rest break plan as the rest break team keeps it. */
data class ScheduledRestBreakPlan(
    var code: String,
    var title: String,
    var pallets: Int,
    var driverId: String,
    var timezone: String,
    var pinned: Boolean,
    var status: ScheduledRestBreakPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRestBreakPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRestBreakPlanStatus): Boolean {
        when (status) {
            ScheduledRestBreakPlanStatus.IDLE -> return next == ScheduledRestBreakPlanStatus.WARMING
            ScheduledRestBreakPlanStatus.WARMING -> {
                when (next) {
                    ScheduledRestBreakPlanStatus.READY, ScheduledRestBreakPlanStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledRestBreakPlanStatus.READY -> {
                when (next) {
                    ScheduledRestBreakPlanStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledRestBreakPlanStatus.DRAINING -> {
                when (next) {
                    ScheduledRestBreakPlanStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledRestBreakPlanStatus) {
        if (!canMoveTo(next)) {
            error("scheduled rest break plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): ScheduledRestBreakPlan {
        return ScheduledRestBreakPlan(
            code = code,
            title = title,
            pallets = pallets,
            driverId = driverId,
            timezone = timezone,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled rest break plan in status idle. */
fun newScheduledRestBreakPlan(code: String, title: String): ScheduledRestBreakPlan {
    return ScheduledRestBreakPlan(
        code = code,
        title = title,
        pallets = 0,
        driverId = "",
        timezone = "",
        pinned = false,
        status = ScheduledRestBreakPlanStatus.IDLE,
    )
}
