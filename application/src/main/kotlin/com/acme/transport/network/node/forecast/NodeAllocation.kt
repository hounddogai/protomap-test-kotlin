package com.acme.transport.network.node.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node allocation is in its life cycle. */
enum class NodeAllocationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a node allocation and its current status. */
data class NodeAllocation(
    var code: String,
    var name: String,
    var endsAt: Int,
    var driverId: String,
    var revision: Int,
    var batchSize: Int,
    var status: NodeAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeAllocationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeAllocationStatus): Boolean {
        when (status) {
            NodeAllocationStatus.CREATED -> return next == NodeAllocationStatus.RETIRED
            NodeAllocationStatus.VALIDATED -> {
                when (next) {
                    NodeAllocationStatus.PUBLISHED, NodeAllocationStatus.RETIRED -> return true
                }
                return false
            }
            NodeAllocationStatus.PUBLISHED -> return next == NodeAllocationStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: NodeAllocationStatus) {
        if (!canMoveTo(next)) {
            error("node allocation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun adjustedValue(): Double = round((revision.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): NodeAllocation {
        return NodeAllocation(
            code = code,
            name = name,
            endsAt = endsAt,
            driverId = driverId,
            revision = revision,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a node allocation in status created. */
fun newNodeAllocation(code: String, name: String): NodeAllocation {
    return NodeAllocation(
        code = code,
        name = name,
        endsAt = 0,
        driverId = "",
        revision = 0,
        batchSize = 0,
        status = NodeAllocationStatus.CREATED,
    )
}
