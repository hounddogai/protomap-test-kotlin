package com.acme.transport.network.cutoff.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped cutoff milestone is in its life cycle. */
enum class CappedCutoffMilestoneStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One capped cutoff milestone, with what the planners need to act on it. */
data class CappedCutoffMilestone(
    var code: String,
    var name: String,
    var vehicleId: String,
    var country: String,
    var updatedAt: Int,
    var graceDays: Int,
    var margin: Double,
    var driverId: String,
    var label: String,
    var cost: Double,
    var status: CappedCutoffMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCutoffMilestoneStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCutoffMilestoneStatus): Boolean {
        when (status) {
            CappedCutoffMilestoneStatus.PROPOSED -> return next == CappedCutoffMilestoneStatus.WITHDRAWN
            CappedCutoffMilestoneStatus.APPROVED -> return next == CappedCutoffMilestoneStatus.WITHDRAWN
            CappedCutoffMilestoneStatus.REJECTED -> return next == CappedCutoffMilestoneStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedCutoffMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("capped cutoff milestone $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $name"

    fun effectiveLoad(): Double = round(graceDays.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): CappedCutoffMilestone {
        return CappedCutoffMilestone(
            code = code,
            name = name,
            vehicleId = vehicleId,
            country = country,
            updatedAt = updatedAt,
            graceDays = graceDays,
            margin = margin,
            driverId = driverId,
            label = label,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped cutoff milestone in status proposed. */
fun newCappedCutoffMilestone(code: String, name: String): CappedCutoffMilestone {
    return CappedCutoffMilestone(
        code = code,
        name = name,
        vehicleId = "",
        country = "",
        updatedAt = 0,
        graceDays = 0,
        margin = 0.0,
        driverId = "",
        label = "",
        cost = 0.0,
        status = CappedCutoffMilestoneStatus.PROPOSED,
    )
}
