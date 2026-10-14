package com.acme.transport.network.node.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node baseline is in its life cycle. */
enum class NodeBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A node baseline as the node team keeps it. */
data class NodeBaseline(
    var code: String,
    var name: String,
    var sequence: Int,
    var minimum: Double,
    var timezone: String,
    var windowDays: Int,
    var channel: String,
    var region: String,
    var capacity: Int,
    var fuelLiters: Double,
    var status: NodeBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeBaselineStatus): Boolean {
        when (status) {
            NodeBaselineStatus.DRAFT -> {
                when (next) {
                    NodeBaselineStatus.ACTIVE, NodeBaselineStatus.SUSPENDED -> return true
                }
                return false
            }
            NodeBaselineStatus.PENDING -> return next == NodeBaselineStatus.SUSPENDED || next == NodeBaselineStatus.CLOSED
            NodeBaselineStatus.ACTIVE -> {
                when (next) {
                    NodeBaselineStatus.SUSPENDED, NodeBaselineStatus.CLOSED -> return true
                }
                return false
            }
            NodeBaselineStatus.SUSPENDED -> return next == NodeBaselineStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: NodeBaselineStatus) {
        if (!canMoveTo(next)) {
            error("node baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedLoad(): Double = round((fuelLiters - sequence.toDouble()) * 100) / 100

    fun snapshot(): NodeBaseline {
        return NodeBaseline(
            code = code,
            name = name,
            sequence = sequence,
            minimum = minimum,
            timezone = timezone,
            windowDays = windowDays,
            channel = channel,
            region = region,
            capacity = capacity,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a node baseline in status draft. */
fun newNodeBaseline(code: String, name: String): NodeBaseline {
    return NodeBaseline(
        code = code,
        name = name,
        sequence = 0,
        minimum = 0.0,
        timezone = "",
        windowDays = 0,
        channel = "",
        region = "",
        capacity = 0,
        fuelLiters = 0.0,
        status = NodeBaselineStatus.DRAFT,
    )
}
