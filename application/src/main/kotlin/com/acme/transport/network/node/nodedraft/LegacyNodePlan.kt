package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a legacy node plan is in its life cycle. */
enum class LegacyNodePlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy node plan as the node team keeps it. */
data class LegacyNodePlan(
    var code: String,
    var name: String,
    var stopCount: Int,
    var carrier: String,
    var priority: Int,
    var country: String,
    var maximum: Double,
    var status: LegacyNodePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyNodePlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyNodePlanStatus): Boolean {
        when (status) {
            LegacyNodePlanStatus.PLANNED -> {
                when (next) {
                    LegacyNodePlanStatus.SCHEDULED, LegacyNodePlanStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LegacyNodePlanStatus.SCHEDULED -> {
                when (next) {
                    LegacyNodePlanStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LegacyNodePlanStatus.IN_PROGRESS -> return next == LegacyNodePlanStatus.COMPLETED || next == LegacyNodePlanStatus.CANCELLED
            LegacyNodePlanStatus.COMPLETED -> return next == LegacyNodePlanStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyNodePlanStatus) {
        if (!canMoveTo(next)) {
            error("legacy node plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedExposure(): Double = round((maximum + priority.toDouble()) * 100) / 100

    fun snapshot(): LegacyNodePlan {
        return LegacyNodePlan(
            code = code,
            name = name,
            stopCount = stopCount,
            carrier = carrier,
            priority = priority,
            country = country,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy node plan in status planned. */
fun newLegacyNodePlan(code: String, name: String): LegacyNodePlan {
    return LegacyNodePlan(
        code = code,
        name = name,
        stopCount = 0,
        carrier = "",
        priority = 0,
        country = "",
        maximum = 0.0,
        status = LegacyNodePlanStatus.PLANNED,
    )
}
