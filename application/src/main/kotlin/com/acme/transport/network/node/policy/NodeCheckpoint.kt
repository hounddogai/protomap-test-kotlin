package com.acme.transport.network.node.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node checkpoint is in its life cycle. */
enum class NodeCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A node checkpoint. Its code is unique within a tenant. */
data class NodeCheckpoint(
    var code: String,
    var name: String,
    var version: Int,
    var score: Double,
    var notes: String,
    var attempts: Int,
    var archived: Boolean,
    var routeCode: String,
    var quantity: Int,
    var status: NodeCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeCheckpointStatus): Boolean {
        when (status) {
            NodeCheckpointStatus.CREATED -> {
                when (next) {
                    NodeCheckpointStatus.PUBLISHED, NodeCheckpointStatus.RETIRED -> return true
                }
                return false
            }
            NodeCheckpointStatus.VALIDATED -> return next == NodeCheckpointStatus.PUBLISHED
            NodeCheckpointStatus.PUBLISHED -> return next == NodeCheckpointStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: NodeCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("node checkpoint $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$name]"

    fun weightedLoad(): Double = round((quantity.toDouble() - score) * 100) / 100

    fun snapshot(): NodeCheckpoint {
        return NodeCheckpoint(
            code = code,
            name = name,
            version = version,
            score = score,
            notes = notes,
            attempts = attempts,
            archived = archived,
            routeCode = routeCode,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a node checkpoint in status created. */
fun newNodeCheckpoint(code: String, name: String): NodeCheckpoint {
    return NodeCheckpoint(
        code = code,
        name = name,
        version = 0,
        score = 0.0,
        notes = "",
        attempts = 0,
        archived = false,
        routeCode = "",
        quantity = 0,
        status = NodeCheckpointStatus.CREATED,
    )
}
