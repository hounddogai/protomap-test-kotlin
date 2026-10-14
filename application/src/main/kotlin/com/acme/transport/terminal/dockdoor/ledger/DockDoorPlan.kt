package com.acme.transport.terminal.dockdoor.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door plan is in its life cycle. */
enum class DockDoorPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One dock door plan, with what the planners need to act on it. */
data class DockDoorPlan(
    var code: String,
    var name: String,
    var lane: String,
    var budget: Double,
    var visible: Boolean,
    var sequence: Int,
    var category: String,
    var vehicleId: String,
    var status: DockDoorPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorPlanStatus): Boolean {
        when (status) {
            DockDoorPlanStatus.IDLE -> return next == DockDoorPlanStatus.WARMING || next == DockDoorPlanStatus.READY
            DockDoorPlanStatus.WARMING -> return next == DockDoorPlanStatus.READY || next == DockDoorPlanStatus.STOPPED
            DockDoorPlanStatus.READY -> {
                when (next) {
                    DockDoorPlanStatus.DRAINING, DockDoorPlanStatus.STOPPED -> return true
                }
                return false
            }
            DockDoorPlanStatus.DRAINING -> return next == DockDoorPlanStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: DockDoorPlanStatus) {
        if (!canMoveTo(next)) {
            error("dock door plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedTotal(): Double = round((budget - sequence.toDouble()) * 100) / 100

    fun snapshot(): DockDoorPlan {
        return DockDoorPlan(
            code = code,
            name = name,
            lane = lane,
            budget = budget,
            visible = visible,
            sequence = sequence,
            category = category,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a dock door plan in status idle. */
fun newDockDoorPlan(code: String, name: String): DockDoorPlan {
    return DockDoorPlan(
        code = code,
        name = name,
        lane = "",
        budget = 0.0,
        visible = false,
        sequence = 0,
        category = "",
        vehicleId = "",
        status = DockDoorPlanStatus.IDLE,
    )
}
