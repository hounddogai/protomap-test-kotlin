package com.acme.transport.network.node.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled node milestone records of network's node work, and the rules, stores, and calculations around
// them.

/** Where a scheduled node milestone is in its life cycle. */
enum class ScheduledNodeMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled node milestone. Its code is unique within a tenant. */
data class ScheduledNodeMilestone(
    var code: String,
    var title: String,
    var trailerId: String,
    var durationMinutes: Int,
    var fuelLiters: Double,
    var ratio: Double,
    var amount: Double,
    var budget: Double,
    var vehicleId: String,
    var status: ScheduledNodeMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledNodeMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledNodeMilestoneStatus): Boolean {
        when (status) {
            ScheduledNodeMilestoneStatus.CREATED -> {
                when (next) {
                    ScheduledNodeMilestoneStatus.PUBLISHED, ScheduledNodeMilestoneStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledNodeMilestoneStatus.VALIDATED -> return next == ScheduledNodeMilestoneStatus.RETIRED
            ScheduledNodeMilestoneStatus.PUBLISHED -> {
                when (next) {
                    ScheduledNodeMilestoneStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledNodeMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("scheduled node milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedLoad(): Double = round((budget - amount) * 100) / 100

    fun snapshot(): ScheduledNodeMilestone {
        return ScheduledNodeMilestone(
            code = code,
            title = title,
            trailerId = trailerId,
            durationMinutes = durationMinutes,
            fuelLiters = fuelLiters,
            ratio = ratio,
            amount = amount,
            budget = budget,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled node milestone in status created. */
fun newScheduledNodeMilestone(code: String, title: String): ScheduledNodeMilestone {
    return ScheduledNodeMilestone(
        code = code,
        title = title,
        trailerId = "",
        durationMinutes = 0,
        fuelLiters = 0.0,
        ratio = 0.0,
        amount = 0.0,
        budget = 0.0,
        vehicleId = "",
        status = ScheduledNodeMilestoneStatus.CREATED,
    )
}
