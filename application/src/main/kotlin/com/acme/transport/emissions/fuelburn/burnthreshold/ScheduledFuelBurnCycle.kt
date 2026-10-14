package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled fuel burn cycle is in its life cycle. */
enum class ScheduledFuelBurnCycleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One scheduled fuel burn cycle, with what the planners need to act on it. */
data class ScheduledFuelBurnCycle(
    var code: String,
    var name: String,
    var category: String,
    var label: String,
    var tags: MutableList<String>,
    var durationMinutes: Int,
    var timezone: String,
    var cost: Double,
    var routeCode: String,
    var active: Boolean,
    var priority: Int,
    var status: ScheduledFuelBurnCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledFuelBurnCycleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledFuelBurnCycleStatus): Boolean {
        when (status) {
            ScheduledFuelBurnCycleStatus.OPEN -> return next == ScheduledFuelBurnCycleStatus.REOPENED
            ScheduledFuelBurnCycleStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledFuelBurnCycleStatus.RESOLVED, ScheduledFuelBurnCycleStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledFuelBurnCycleStatus.RESOLVED -> {
                when (next) {
                    ScheduledFuelBurnCycleStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledFuelBurnCycleStatus.REOPENED -> return next == ScheduledFuelBurnCycleStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledFuelBurnCycleStatus) {
        if (!canMoveTo(next)) {
            error("scheduled fuel burn cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled fuel burn cycle"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round((cost + priority.toDouble()) * 100) / 100

    fun snapshot(): ScheduledFuelBurnCycle {
        return ScheduledFuelBurnCycle(
            code = code,
            name = name,
            category = category,
            label = label,
            tags = tags,
            durationMinutes = durationMinutes,
            timezone = timezone,
            cost = cost,
            routeCode = routeCode,
            active = active,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled fuel burn cycle in status open. */
fun newScheduledFuelBurnCycle(code: String, name: String): ScheduledFuelBurnCycle {
    return ScheduledFuelBurnCycle(
        code = code,
        name = name,
        category = "",
        label = "",
        tags = mutableListOf<String>(),
        durationMinutes = 0,
        timezone = "",
        cost = 0.0,
        routeCode = "",
        active = false,
        priority = 0,
        status = ScheduledFuelBurnCycleStatus.OPEN,
    )
}
