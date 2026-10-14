package com.acme.transport.network.node.policy

import com.acme.transport.kit.text.normalize

/** Where a node window is in its life cycle. */
enum class NodeWindowStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One node window, with what the planners need to act on it. */
data class NodeWindow(
    var code: String,
    var name: String,
    var stopCount: Int,
    var currency: String,
    var driverId: String,
    var description: String,
    var status: NodeWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeWindowStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeWindowStatus): Boolean {
        when (status) {
            NodeWindowStatus.DRAFT -> return next == NodeWindowStatus.ACTIVE || next == NodeWindowStatus.SUSPENDED
            NodeWindowStatus.PENDING -> {
                when (next) {
                    NodeWindowStatus.ACTIVE -> return true
                }
                return false
            }
            NodeWindowStatus.ACTIVE -> return next == NodeWindowStatus.SUSPENDED || next == NodeWindowStatus.CLOSED
            NodeWindowStatus.SUSPENDED -> {
                when (next) {
                    NodeWindowStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeWindowStatus) {
        if (!canMoveTo(next)) {
            error("node window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): NodeWindow {
        return NodeWindow(
            code = code,
            name = name,
            stopCount = stopCount,
            currency = currency,
            driverId = driverId,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a node window in status draft. */
fun newNodeWindow(code: String, name: String): NodeWindow {
    return NodeWindow(
        code = code,
        name = name,
        stopCount = 0,
        currency = "",
        driverId = "",
        description = "",
        status = NodeWindowStatus.DRAFT,
    )
}
