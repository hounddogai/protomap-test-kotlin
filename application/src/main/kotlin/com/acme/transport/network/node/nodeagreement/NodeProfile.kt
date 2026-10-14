package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.text.normalize

/** Where a node profile is in its life cycle. */
enum class NodeProfileStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A node profile. Its code is unique within a tenant. */
data class NodeProfile(
    var code: String,
    var title: String,
    var currency: String,
    var ownerId: String,
    var maximum: Double,
    var status: NodeProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeProfileStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeProfileStatus): Boolean {
        when (status) {
            NodeProfileStatus.REQUESTED -> return next == NodeProfileStatus.GRANTED || next == NodeProfileStatus.EXPIRED
            NodeProfileStatus.GRANTED -> {
                when (next) {
                    NodeProfileStatus.REVOKED -> return true
                }
                return false
            }
            NodeProfileStatus.REVOKED -> {
                when (next) {
                    NodeProfileStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeProfileStatus) {
        if (!canMoveTo(next)) {
            error("node profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"node profile"} $code"

    fun measuredValue(): Double = maximum

    fun snapshot(): NodeProfile {
        return NodeProfile(
            code = code,
            title = title,
            currency = currency,
            ownerId = ownerId,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a node profile in status requested. */
fun newNodeProfile(code: String, title: String): NodeProfile {
    return NodeProfile(
        code = code,
        title = title,
        currency = "",
        ownerId = "",
        maximum = 0.0,
        status = NodeProfileStatus.REQUESTED,
    )
}
