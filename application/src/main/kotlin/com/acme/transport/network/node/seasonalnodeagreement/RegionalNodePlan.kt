package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional node plan is in its life cycle. */
enum class RegionalNodePlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A regional node plan as the node team keeps it. */
data class RegionalNodePlan(
    var code: String,
    var title: String,
    var odometer: Int,
    var pallets: Int,
    var lane: String,
    var visible: Boolean,
    var cost: Double,
    var priority: Int,
    var limit: Int,
    var status: RegionalNodePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalNodePlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalNodePlanStatus): Boolean {
        when (status) {
            RegionalNodePlanStatus.IDLE -> return next == RegionalNodePlanStatus.WARMING
            RegionalNodePlanStatus.WARMING -> return next == RegionalNodePlanStatus.STOPPED
            RegionalNodePlanStatus.READY -> return next == RegionalNodePlanStatus.DRAINING || next == RegionalNodePlanStatus.STOPPED
            RegionalNodePlanStatus.DRAINING -> {
                when (next) {
                    RegionalNodePlanStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalNodePlanStatus) {
        if (!canMoveTo(next)) {
            error("regional node plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedTotal(): Double = round((odometer.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): RegionalNodePlan {
        return RegionalNodePlan(
            code = code,
            title = title,
            odometer = odometer,
            pallets = pallets,
            lane = lane,
            visible = visible,
            cost = cost,
            priority = priority,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a regional node plan in status idle. */
fun newRegionalNodePlan(code: String, title: String): RegionalNodePlan {
    return RegionalNodePlan(
        code = code,
        title = title,
        odometer = 0,
        pallets = 0,
        lane = "",
        visible = false,
        cost = 0.0,
        priority = 0,
        limit = 0,
        status = RegionalNodePlanStatus.IDLE,
    )
}
