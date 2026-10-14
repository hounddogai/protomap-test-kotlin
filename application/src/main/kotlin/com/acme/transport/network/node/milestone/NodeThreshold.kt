package com.acme.transport.network.node.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node threshold is in its life cycle. */
enum class NodeThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A node threshold. Its code is unique within a tenant. */
data class NodeThreshold(
    var code: String,
    var name: String,
    var pallets: Int,
    var durationMinutes: Int,
    var maximum: Double,
    var unit: String,
    var distanceKm: Double,
    var status: NodeThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeThresholdStatus): Boolean {
        when (status) {
            NodeThresholdStatus.OPEN -> {
                when (next) {
                    NodeThresholdStatus.RESOLVED -> return true
                }
                return false
            }
            NodeThresholdStatus.IN_REVIEW -> {
                when (next) {
                    NodeThresholdStatus.REOPENED, NodeThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
            NodeThresholdStatus.RESOLVED -> {
                when (next) {
                    NodeThresholdStatus.REOPENED, NodeThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
            NodeThresholdStatus.REOPENED -> {
                when (next) {
                    NodeThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeThresholdStatus) {
        if (!canMoveTo(next)) {
            error("node threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"node threshold"} $code"

    fun projectedVolume(): Double = round((maximum + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): NodeThreshold {
        return NodeThreshold(
            code = code,
            name = name,
            pallets = pallets,
            durationMinutes = durationMinutes,
            maximum = maximum,
            unit = unit,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a node threshold in status open. */
fun newNodeThreshold(code: String, name: String): NodeThreshold {
    return NodeThreshold(
        code = code,
        name = name,
        pallets = 0,
        durationMinutes = 0,
        maximum = 0.0,
        unit = "",
        distanceKm = 0.0,
        status = NodeThresholdStatus.OPEN,
    )
}
