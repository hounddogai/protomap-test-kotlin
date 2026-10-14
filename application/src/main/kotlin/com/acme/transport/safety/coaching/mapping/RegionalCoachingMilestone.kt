package com.acme.transport.safety.coaching.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional coaching milestone is in its life cycle. */
enum class RegionalCoachingMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A regional coaching milestone. Its code is unique within a tenant. */
data class RegionalCoachingMilestone(
    var code: String,
    var name: String,
    var summary: String,
    var phone: String,
    var channel: String,
    var unit: String,
    var budget: Double,
    var tags: MutableList<String>,
    var pallets: Int,
    var currency: String,
    var approved: Boolean,
    var status: RegionalCoachingMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCoachingMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCoachingMilestoneStatus): Boolean {
        when (status) {
            RegionalCoachingMilestoneStatus.PLANNED -> {
                when (next) {
                    RegionalCoachingMilestoneStatus.SCHEDULED, RegionalCoachingMilestoneStatus.COMPLETED -> return true
                }
                return false
            }
            RegionalCoachingMilestoneStatus.SCHEDULED -> return next == RegionalCoachingMilestoneStatus.IN_PROGRESS
            RegionalCoachingMilestoneStatus.IN_PROGRESS -> {
                when (next) {
                    RegionalCoachingMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalCoachingMilestoneStatus.COMPLETED -> {
                when (next) {
                    RegionalCoachingMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCoachingMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("regional coaching milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedTotal(): Double = round(budget * pallets.toDouble() * 100) / 100

    fun snapshot(): RegionalCoachingMilestone {
        return RegionalCoachingMilestone(
            code = code,
            name = name,
            summary = summary,
            phone = phone,
            channel = channel,
            unit = unit,
            budget = budget,
            tags = tags,
            pallets = pallets,
            currency = currency,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a regional coaching milestone in status planned. */
fun newRegionalCoachingMilestone(code: String, name: String): RegionalCoachingMilestone {
    return RegionalCoachingMilestone(
        code = code,
        name = name,
        summary = "",
        phone = "",
        channel = "",
        unit = "",
        budget = 0.0,
        tags = mutableListOf<String>(),
        pallets = 0,
        currency = "",
        approved = false,
        status = RegionalCoachingMilestoneStatus.PLANNED,
    )
}
