package com.acme.transport.network.node.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The node cycle records of network's node work, and the rules, stores, and calculations around them.

/** Where a node cycle is in its life cycle. */
enum class NodeCycleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A node cycle as the node team keeps it. */
data class NodeCycle(
    var code: String,
    var title: String,
    var active: Boolean,
    var graceDays: Int,
    var discount: Double,
    var unit: String,
    var windowDays: Int,
    var visible: Boolean,
    var status: NodeCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeCycleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeCycleStatus): Boolean {
        when (status) {
            NodeCycleStatus.PROPOSED -> {
                when (next) {
                    NodeCycleStatus.REJECTED, NodeCycleStatus.WITHDRAWN -> return true
                }
                return false
            }
            NodeCycleStatus.APPROVED -> return next == NodeCycleStatus.WITHDRAWN
            NodeCycleStatus.REJECTED -> return next == NodeCycleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: NodeCycleStatus) {
        if (!canMoveTo(next)) {
            error("node cycle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedExposure(): Double = round((discount + windowDays.toDouble()) * 100) / 100

    fun snapshot(): NodeCycle {
        return NodeCycle(
            code = code,
            title = title,
            active = active,
            graceDays = graceDays,
            discount = discount,
            unit = unit,
            windowDays = windowDays,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a node cycle in status proposed. */
fun newNodeCycle(code: String, title: String): NodeCycle {
    return NodeCycle(
        code = code,
        title = title,
        active = false,
        graceDays = 0,
        discount = 0.0,
        unit = "",
        windowDays = 0,
        visible = false,
        status = NodeCycleStatus.PROPOSED,
    )
}
