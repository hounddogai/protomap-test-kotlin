package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled utilization milestone records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a scheduled utilization milestone is in its life cycle. */
enum class ScheduledUtilizationMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scheduled utilization milestone, with what the planners need to act on it. */
data class ScheduledUtilizationMilestone(
    var code: String,
    var name: String,
    var category: String,
    var endsAt: Int,
    var trailerId: String,
    var createdAt: Int,
    var attempts: Int,
    var cost: Double,
    var pinned: Boolean,
    var shiftCode: String,
    var status: ScheduledUtilizationMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledUtilizationMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledUtilizationMilestoneStatus): Boolean {
        when (status) {
            ScheduledUtilizationMilestoneStatus.CREATED -> return next == ScheduledUtilizationMilestoneStatus.VALIDATED || next == ScheduledUtilizationMilestoneStatus.RETIRED
            ScheduledUtilizationMilestoneStatus.VALIDATED -> return next == ScheduledUtilizationMilestoneStatus.RETIRED
            ScheduledUtilizationMilestoneStatus.PUBLISHED -> {
                when (next) {
                    ScheduledUtilizationMilestoneStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledUtilizationMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("scheduled utilization milestone $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun weightedExposure(): Double = round((attempts.toDouble() - cost) * 100) / 100

    fun snapshot(): ScheduledUtilizationMilestone {
        return ScheduledUtilizationMilestone(
            code = code,
            name = name,
            category = category,
            endsAt = endsAt,
            trailerId = trailerId,
            createdAt = createdAt,
            attempts = attempts,
            cost = cost,
            pinned = pinned,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled utilization milestone in status created. */
fun newScheduledUtilizationMilestone(code: String, name: String): ScheduledUtilizationMilestone {
    return ScheduledUtilizationMilestone(
        code = code,
        name = name,
        category = "",
        endsAt = 0,
        trailerId = "",
        createdAt = 0,
        attempts = 0,
        cost = 0.0,
        pinned = false,
        shiftCode = "",
        status = ScheduledUtilizationMilestoneStatus.CREATED,
    )
}
