package com.acme.transport.terminal.dwell.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional dwell plan records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a regional dwell plan is in its life cycle. */
enum class RegionalDwellPlanStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One regional dwell plan, with what the planners need to act on it. */
data class RegionalDwellPlan(
    var code: String,
    var title: String,
    var stopCount: Int,
    var vehicleId: String,
    var phone: String,
    var tags: MutableList<String>,
    var capacity: Int,
    var ceiling: Double,
    var priority: Int,
    var status: RegionalDwellPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDwellPlanStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDwellPlanStatus): Boolean {
        when (status) {
            RegionalDwellPlanStatus.DRAFT -> {
                when (next) {
                    RegionalDwellPlanStatus.PENDING, RegionalDwellPlanStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalDwellPlanStatus.PENDING -> {
                when (next) {
                    RegionalDwellPlanStatus.ACTIVE -> return true
                }
                return false
            }
            RegionalDwellPlanStatus.ACTIVE -> return next == RegionalDwellPlanStatus.CLOSED
            RegionalDwellPlanStatus.SUSPENDED -> {
                when (next) {
                    RegionalDwellPlanStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDwellPlanStatus) {
        if (!canMoveTo(next)) {
            error("regional dwell plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedVolume(): Double = round(ceiling * priority.toDouble() * 100) / 100

    fun snapshot(): RegionalDwellPlan {
        return RegionalDwellPlan(
            code = code,
            title = title,
            stopCount = stopCount,
            vehicleId = vehicleId,
            phone = phone,
            tags = tags,
            capacity = capacity,
            ceiling = ceiling,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional dwell plan in status draft. */
fun newRegionalDwellPlan(code: String, title: String): RegionalDwellPlan {
    return RegionalDwellPlan(
        code = code,
        title = title,
        stopCount = 0,
        vehicleId = "",
        phone = "",
        tags = mutableListOf<String>(),
        capacity = 0,
        ceiling = 0.0,
        priority = 0,
        status = RegionalDwellPlanStatus.DRAFT,
    )
}
