package com.acme.transport.network.node.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node request is in its life cycle. */
enum class NodeRequestStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One node request, with what the planners need to act on it. */
data class NodeRequest(
    var code: String,
    var label: String,
    var budget: Double,
    var loadKg: Double,
    var driverId: String,
    var status: NodeRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeRequestStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeRequestStatus): Boolean {
        when (status) {
            NodeRequestStatus.DRAFT -> return next == NodeRequestStatus.ACTIVE
            NodeRequestStatus.PENDING -> {
                when (next) {
                    NodeRequestStatus.SUSPENDED -> return true
                }
                return false
            }
            NodeRequestStatus.ACTIVE -> return next == NodeRequestStatus.CLOSED
            NodeRequestStatus.SUSPENDED -> {
                when (next) {
                    NodeRequestStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeRequestStatus) {
        if (!canMoveTo(next)) {
            error("node request $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedVolume(): Double = round((loadKg - budget) * 100) / 100

    fun snapshot(): NodeRequest {
        return NodeRequest(
            code = code,
            label = label,
            budget = budget,
            loadKg = loadKg,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a node request in status draft. */
fun newNodeRequest(code: String, label: String): NodeRequest {
    return NodeRequest(
        code = code,
        label = label,
        budget = 0.0,
        loadKg = 0.0,
        driverId = "",
        status = NodeRequestStatus.DRAFT,
    )
}
