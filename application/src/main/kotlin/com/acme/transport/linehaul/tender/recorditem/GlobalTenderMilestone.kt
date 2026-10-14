package com.acme.transport.linehaul.tender.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global tender milestone is in its life cycle. */
enum class GlobalTenderMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A global tender milestone as the tender team keeps it. */
data class GlobalTenderMilestone(
    var code: String,
    var label: String,
    var cost: Double,
    var channel: String,
    var ceiling: Double,
    var carrier: String,
    var lane: String,
    var status: GlobalTenderMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTenderMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTenderMilestoneStatus): Boolean {
        when (status) {
            GlobalTenderMilestoneStatus.IDLE -> return next == GlobalTenderMilestoneStatus.WARMING || next == GlobalTenderMilestoneStatus.DRAINING
            GlobalTenderMilestoneStatus.WARMING -> return next == GlobalTenderMilestoneStatus.READY || next == GlobalTenderMilestoneStatus.STOPPED
            GlobalTenderMilestoneStatus.READY -> return next == GlobalTenderMilestoneStatus.DRAINING || next == GlobalTenderMilestoneStatus.STOPPED
            GlobalTenderMilestoneStatus.DRAINING -> return next == GlobalTenderMilestoneStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: GlobalTenderMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("global tender milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedLoad(): Double = round(ceiling * cost * 100) / 100

    fun snapshot(): GlobalTenderMilestone {
        return GlobalTenderMilestone(
            code = code,
            label = label,
            cost = cost,
            channel = channel,
            ceiling = ceiling,
            carrier = carrier,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a global tender milestone in status idle. */
fun newGlobalTenderMilestone(code: String, label: String): GlobalTenderMilestone {
    return GlobalTenderMilestone(
        code = code,
        label = label,
        cost = 0.0,
        channel = "",
        ceiling = 0.0,
        carrier = "",
        lane = "",
        status = GlobalTenderMilestoneStatus.IDLE,
    )
}
