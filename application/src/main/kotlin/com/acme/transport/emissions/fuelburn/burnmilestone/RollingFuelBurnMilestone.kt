package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling fuel burn milestone records of emissions's fuel burn work, and the rules, stores, and
// calculations around them.

/** Where a rolling fuel burn milestone is in its life cycle. */
enum class RollingFuelBurnMilestoneStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rolling fuel burn milestone as the fuel burn team keeps it. */
data class RollingFuelBurnMilestone(
    var code: String,
    var title: String,
    var carrier: String,
    var distanceKm: Double,
    var phone: String,
    var updatedAt: Int,
    var status: RollingFuelBurnMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingFuelBurnMilestoneStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingFuelBurnMilestoneStatus): Boolean {
        when (status) {
            RollingFuelBurnMilestoneStatus.PROPOSED -> return next == RollingFuelBurnMilestoneStatus.REJECTED
            RollingFuelBurnMilestoneStatus.APPROVED -> return next == RollingFuelBurnMilestoneStatus.WITHDRAWN
            RollingFuelBurnMilestoneStatus.REJECTED -> return next == RollingFuelBurnMilestoneStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingFuelBurnMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("rolling fuel burn milestone $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveLoad(): Double = round((distanceKm + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): RollingFuelBurnMilestone {
        return RollingFuelBurnMilestone(
            code = code,
            title = title,
            carrier = carrier,
            distanceKm = distanceKm,
            phone = phone,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling fuel burn milestone in status proposed. */
fun newRollingFuelBurnMilestone(code: String, title: String): RollingFuelBurnMilestone {
    return RollingFuelBurnMilestone(
        code = code,
        title = title,
        carrier = "",
        distanceKm = 0.0,
        phone = "",
        updatedAt = 0,
        status = RollingFuelBurnMilestoneStatus.PROPOSED,
    )
}
