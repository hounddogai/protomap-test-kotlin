package com.acme.transport.network.node.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node target is in its life cycle. */
enum class NodeTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One node target, with what the planners need to act on it. */
data class NodeTarget(
    var code: String,
    var name: String,
    var loadKg: Double,
    var routeCode: String,
    var odometer: Int,
    var status: NodeTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeTargetStatus): Boolean {
        when (status) {
            NodeTargetStatus.OPEN -> return next == NodeTargetStatus.RESOLVED
            NodeTargetStatus.IN_REVIEW -> return next == NodeTargetStatus.ARCHIVED
            NodeTargetStatus.RESOLVED -> {
                when (next) {
                    NodeTargetStatus.ARCHIVED -> return true
                }
                return false
            }
            NodeTargetStatus.REOPENED -> return next == NodeTargetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: NodeTargetStatus) {
        if (!canMoveTo(next)) {
            error("node target $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveValue(): Double = round((loadKg - odometer.toDouble()) * 100) / 100

    fun snapshot(): NodeTarget {
        return NodeTarget(
            code = code,
            name = name,
            loadKg = loadKg,
            routeCode = routeCode,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a node target in status open. */
fun newNodeTarget(code: String, name: String): NodeTarget {
    return NodeTarget(
        code = code,
        name = name,
        loadKg = 0.0,
        routeCode = "",
        odometer = 0,
        status = NodeTargetStatus.OPEN,
    )
}
