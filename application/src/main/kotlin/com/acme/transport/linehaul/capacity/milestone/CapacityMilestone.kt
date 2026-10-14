package com.acme.transport.linehaul.capacity.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capacity milestone records of linehaul's capacity work, and the rules, stores, and calculations around
// them.

/** Where a capacity milestone is in its life cycle. */
enum class CapacityMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a capacity milestone and its current status. */
data class CapacityMilestone(
    var code: String,
    var name: String,
    var cost: Double,
    var loadKg: Double,
    var floorValue: Double,
    var budget: Double,
    var approved: Boolean,
    var contactEmail: String,
    var capacity: Int,
    var etaMinutes: Int,
    var description: String,
    var status: CapacityMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityMilestoneStatus): Boolean {
        when (status) {
            CapacityMilestoneStatus.PLANNED -> return next == CapacityMilestoneStatus.COMPLETED || next == CapacityMilestoneStatus.CANCELLED
            CapacityMilestoneStatus.SCHEDULED -> {
                when (next) {
                    CapacityMilestoneStatus.COMPLETED, CapacityMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            CapacityMilestoneStatus.IN_PROGRESS -> {
                when (next) {
                    CapacityMilestoneStatus.COMPLETED, CapacityMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            CapacityMilestoneStatus.COMPLETED -> return next == CapacityMilestoneStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CapacityMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("capacity milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedValue(): Double = round((budget + loadKg) * 100) / 100

    fun snapshot(): CapacityMilestone {
        return CapacityMilestone(
            code = code,
            name = name,
            cost = cost,
            loadKg = loadKg,
            floorValue = floorValue,
            budget = budget,
            approved = approved,
            contactEmail = contactEmail,
            capacity = capacity,
            etaMinutes = etaMinutes,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capacity milestone in status planned. */
fun newCapacityMilestone(code: String, name: String): CapacityMilestone {
    return CapacityMilestone(
        code = code,
        name = name,
        cost = 0.0,
        loadKg = 0.0,
        floorValue = 0.0,
        budget = 0.0,
        approved = false,
        contactEmail = "",
        capacity = 0,
        etaMinutes = 0,
        description = "",
        status = CapacityMilestoneStatus.PLANNED,
    )
}
