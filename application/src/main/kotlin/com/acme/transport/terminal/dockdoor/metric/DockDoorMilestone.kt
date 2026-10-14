package com.acme.transport.terminal.dockdoor.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door milestone is in its life cycle. */
enum class DockDoorMilestoneStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One dock door milestone, with what the planners need to act on it. */
data class DockDoorMilestone(
    var code: String,
    var title: String,
    var weight: Double,
    var ratio: Double,
    var discount: Double,
    var depot: String,
    var unit: String,
    var currency: String,
    var status: DockDoorMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorMilestoneStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorMilestoneStatus): Boolean {
        when (status) {
            DockDoorMilestoneStatus.PROPOSED -> return next == DockDoorMilestoneStatus.WITHDRAWN
            DockDoorMilestoneStatus.APPROVED -> {
                when (next) {
                    DockDoorMilestoneStatus.REJECTED, DockDoorMilestoneStatus.WITHDRAWN -> return true
                }
                return false
            }
            DockDoorMilestoneStatus.REJECTED -> return next == DockDoorMilestoneStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: DockDoorMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("dock door milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedLoad(): Double = round(ratio * weight * 100) / 100

    fun snapshot(): DockDoorMilestone {
        return DockDoorMilestone(
            code = code,
            title = title,
            weight = weight,
            ratio = ratio,
            discount = discount,
            depot = depot,
            unit = unit,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a dock door milestone in status proposed. */
fun newDockDoorMilestone(code: String, title: String): DockDoorMilestone {
    return DockDoorMilestone(
        code = code,
        title = title,
        weight = 0.0,
        ratio = 0.0,
        discount = 0.0,
        depot = "",
        unit = "",
        currency = "",
        status = DockDoorMilestoneStatus.PROPOSED,
    )
}
