package com.acme.transport.network.node.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node claim is in its life cycle. */
enum class NodeClaimStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a node claim and its current status. */
data class NodeClaim(
    var code: String,
    var name: String,
    var revision: Int,
    var loadKg: Double,
    var notes: String,
    var rate: Double,
    var status: NodeClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeClaimStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeClaimStatus): Boolean {
        when (status) {
            NodeClaimStatus.PLANNED -> return next == NodeClaimStatus.SCHEDULED || next == NodeClaimStatus.CANCELLED
            NodeClaimStatus.SCHEDULED -> {
                when (next) {
                    NodeClaimStatus.IN_PROGRESS, NodeClaimStatus.CANCELLED -> return true
                }
                return false
            }
            NodeClaimStatus.IN_PROGRESS -> {
                when (next) {
                    NodeClaimStatus.CANCELLED -> return true
                }
                return false
            }
            NodeClaimStatus.COMPLETED -> return next == NodeClaimStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: NodeClaimStatus) {
        if (!canMoveTo(next)) {
            error("node claim $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun weightedVolume(): Double = round(revision.toDouble() * loadKg * 100) / 100

    fun snapshot(): NodeClaim {
        return NodeClaim(
            code = code,
            name = name,
            revision = revision,
            loadKg = loadKg,
            notes = notes,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a node claim in status planned. */
fun newNodeClaim(code: String, name: String): NodeClaim {
    return NodeClaim(
        code = code,
        name = name,
        revision = 0,
        loadKg = 0.0,
        notes = "",
        rate = 0.0,
        status = NodeClaimStatus.PLANNED,
    )
}
