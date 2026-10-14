package com.acme.transport.network.node.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The node draft records of network's node work, and the rules, stores, and calculations around them.

/** Where a node draft is in its life cycle. */
enum class NodeDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One node draft, with what the planners need to act on it. */
data class NodeDraft(
    var code: String,
    var name: String,
    var description: String,
    var sequence: Int,
    var pallets: Int,
    var status: NodeDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeDraftStatus): Boolean {
        when (status) {
            NodeDraftStatus.REQUESTED -> return next == NodeDraftStatus.REVOKED
            NodeDraftStatus.GRANTED -> {
                when (next) {
                    NodeDraftStatus.REVOKED, NodeDraftStatus.EXPIRED -> return true
                }
                return false
            }
            NodeDraftStatus.REVOKED -> return next == NodeDraftStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: NodeDraftStatus) {
        if (!canMoveTo(next)) {
            error("node draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"node draft"} $code"

    fun effectiveExposure(): Double = round((sequence.toDouble() + pallets.toDouble()) * 100) / 100

    fun snapshot(): NodeDraft {
        return NodeDraft(
            code = code,
            name = name,
            description = description,
            sequence = sequence,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a node draft in status requested. */
fun newNodeDraft(code: String, name: String): NodeDraft {
    return NodeDraft(
        code = code,
        name = name,
        description = "",
        sequence = 0,
        pallets = 0,
        status = NodeDraftStatus.REQUESTED,
    )
}
