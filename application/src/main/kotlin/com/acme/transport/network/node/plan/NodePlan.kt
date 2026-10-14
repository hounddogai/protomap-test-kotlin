package com.acme.transport.network.node.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The node plan records of network's node work, and the rules, stores, and calculations around them.

/** Where a node plan is in its life cycle. */
enum class NodePlanStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A node plan. Its code is unique within a tenant. */
data class NodePlan(
    var code: String,
    var name: String,
    var timezone: String,
    var vehicleId: String,
    var retries: Int,
    var amount: Double,
    var batchSize: Int,
    var status: NodePlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodePlanStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodePlanStatus): Boolean {
        when (status) {
            NodePlanStatus.PLANNED -> {
                when (next) {
                    NodePlanStatus.IN_PROGRESS, NodePlanStatus.COMPLETED -> return true
                }
                return false
            }
            NodePlanStatus.SCHEDULED -> return next == NodePlanStatus.IN_PROGRESS || next == NodePlanStatus.COMPLETED
            NodePlanStatus.IN_PROGRESS -> {
                when (next) {
                    NodePlanStatus.COMPLETED, NodePlanStatus.CANCELLED -> return true
                }
                return false
            }
            NodePlanStatus.COMPLETED -> {
                when (next) {
                    NodePlanStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodePlanStatus) {
        if (!canMoveTo(next)) {
            error("node plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"node plan"} $code"

    fun netVolume(): Double = round((retries.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): NodePlan {
        return NodePlan(
            code = code,
            name = name,
            timezone = timezone,
            vehicleId = vehicleId,
            retries = retries,
            amount = amount,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a node plan in status planned. */
fun newNodePlan(code: String, name: String): NodePlan {
    return NodePlan(
        code = code,
        name = name,
        timezone = "",
        vehicleId = "",
        retries = 0,
        amount = 0.0,
        batchSize = 0,
        status = NodePlanStatus.PLANNED,
    )
}
