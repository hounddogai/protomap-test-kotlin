package com.acme.transport.network.node.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node slot is in its life cycle. */
enum class NodeSlotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A node slot. Its code is unique within a tenant. */
data class NodeSlot(
    var code: String,
    var label: String,
    var amount: Double,
    var discount: Double,
    var expiresAt: Int,
    var durationMinutes: Int,
    var status: NodeSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeSlotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeSlotStatus): Boolean {
        when (status) {
            NodeSlotStatus.REQUESTED -> {
                when (next) {
                    NodeSlotStatus.GRANTED, NodeSlotStatus.EXPIRED -> return true
                }
                return false
            }
            NodeSlotStatus.GRANTED -> return next == NodeSlotStatus.REVOKED || next == NodeSlotStatus.EXPIRED
            NodeSlotStatus.REVOKED -> {
                when (next) {
                    NodeSlotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeSlotStatus) {
        if (!canMoveTo(next)) {
            error("node slot $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"node slot"} $code"

    fun projectedExposure(): Double = round((amount + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): NodeSlot {
        return NodeSlot(
            code = code,
            label = label,
            amount = amount,
            discount = discount,
            expiresAt = expiresAt,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a node slot in status requested. */
fun newNodeSlot(code: String, label: String): NodeSlot {
    return NodeSlot(
        code = code,
        label = label,
        amount = 0.0,
        discount = 0.0,
        expiresAt = 0,
        durationMinutes = 0,
        status = NodeSlotStatus.REQUESTED,
    )
}
