package com.acme.transport.network.node.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node entry is in its life cycle. */
enum class NodeEntryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A node entry. Its code is unique within a tenant. */
data class NodeEntry(
    var code: String,
    var name: String,
    var retries: Int,
    var threshold: Double,
    var fuelLiters: Double,
    var discount: Double,
    var amount: Double,
    var status: NodeEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeEntryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeEntryStatus): Boolean {
        when (status) {
            NodeEntryStatus.PLANNED -> return next == NodeEntryStatus.COMPLETED
            NodeEntryStatus.SCHEDULED -> return next == NodeEntryStatus.COMPLETED || next == NodeEntryStatus.CANCELLED
            NodeEntryStatus.IN_PROGRESS -> return next == NodeEntryStatus.COMPLETED || next == NodeEntryStatus.CANCELLED
            NodeEntryStatus.COMPLETED -> {
                when (next) {
                    NodeEntryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeEntryStatus) {
        if (!canMoveTo(next)) {
            error("node entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveTotal(): Double = round(fuelLiters * amount * 100) / 100

    fun snapshot(): NodeEntry {
        return NodeEntry(
            code = code,
            name = name,
            retries = retries,
            threshold = threshold,
            fuelLiters = fuelLiters,
            discount = discount,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a node entry in status planned. */
fun newNodeEntry(code: String, name: String): NodeEntry {
    return NodeEntry(
        code = code,
        name = name,
        retries = 0,
        threshold = 0.0,
        fuelLiters = 0.0,
        discount = 0.0,
        amount = 0.0,
        status = NodeEntryStatus.PLANNED,
    )
}
