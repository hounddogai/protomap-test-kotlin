package com.acme.transport.network.node.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node group is in its life cycle. */
enum class NodeGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A node group as the node team keeps it. */
data class NodeGroup(
    var code: String,
    var label: String,
    var phone: String,
    var approved: Boolean,
    var shiftCode: String,
    var etaMinutes: Int,
    var notes: String,
    var threshold: Double,
    var weight: Double,
    var carrier: String,
    var floorValue: Double,
    var status: NodeGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeGroupStatus): Boolean {
        when (status) {
            NodeGroupStatus.PLANNED -> {
                when (next) {
                    NodeGroupStatus.COMPLETED -> return true
                }
                return false
            }
            NodeGroupStatus.SCHEDULED -> {
                when (next) {
                    NodeGroupStatus.IN_PROGRESS -> return true
                }
                return false
            }
            NodeGroupStatus.IN_PROGRESS -> {
                when (next) {
                    NodeGroupStatus.COMPLETED, NodeGroupStatus.CANCELLED -> return true
                }
                return false
            }
            NodeGroupStatus.COMPLETED -> {
                when (next) {
                    NodeGroupStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeGroupStatus) {
        if (!canMoveTo(next)) {
            error("node group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedTotal(): Double = round((etaMinutes.toDouble() + threshold) * 100) / 100

    fun snapshot(): NodeGroup {
        return NodeGroup(
            code = code,
            label = label,
            phone = phone,
            approved = approved,
            shiftCode = shiftCode,
            etaMinutes = etaMinutes,
            notes = notes,
            threshold = threshold,
            weight = weight,
            carrier = carrier,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a node group in status planned. */
fun newNodeGroup(code: String, label: String): NodeGroup {
    return NodeGroup(
        code = code,
        label = label,
        phone = "",
        approved = false,
        shiftCode = "",
        etaMinutes = 0,
        notes = "",
        threshold = 0.0,
        weight = 0.0,
        carrier = "",
        floorValue = 0.0,
        status = NodeGroupStatus.PLANNED,
    )
}
