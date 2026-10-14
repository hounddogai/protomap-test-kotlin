package com.acme.transport.network.node.forecast

import com.acme.transport.kit.text.normalize

// The node forecast records of network's node work, and the rules, stores, and calculations around them.

/** Where a node forecast is in its life cycle. */
enum class NodeForecastStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A node forecast. Its code is unique within a tenant. */
data class NodeForecast(
    var code: String,
    var label: String,
    var lane: String,
    var country: String,
    var createdAt: Int,
    var status: NodeForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeForecastStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeForecastStatus): Boolean {
        when (status) {
            NodeForecastStatus.DRAFT -> {
                when (next) {
                    NodeForecastStatus.PENDING, NodeForecastStatus.ACTIVE -> return true
                }
                return false
            }
            NodeForecastStatus.PENDING -> {
                when (next) {
                    NodeForecastStatus.CLOSED -> return true
                }
                return false
            }
            NodeForecastStatus.ACTIVE -> return next == NodeForecastStatus.SUSPENDED || next == NodeForecastStatus.CLOSED
            NodeForecastStatus.SUSPENDED -> return next == NodeForecastStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: NodeForecastStatus) {
        if (!canMoveTo(next)) {
            error("node forecast $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"node forecast"} $code"

    fun measuredValue(): Double = createdAt.toDouble()

    fun snapshot(): NodeForecast {
        return NodeForecast(
            code = code,
            label = label,
            lane = lane,
            country = country,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a node forecast in status draft. */
fun newNodeForecast(code: String, label: String): NodeForecast {
    return NodeForecast(
        code = code,
        label = label,
        lane = "",
        country = "",
        createdAt = 0,
        status = NodeForecastStatus.DRAFT,
    )
}
