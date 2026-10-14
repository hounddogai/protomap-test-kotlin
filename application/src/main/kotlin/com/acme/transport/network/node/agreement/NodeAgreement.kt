package com.acme.transport.network.node.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The node agreement records of network's node work, and the rules, stores, and calculations around them.

/** Where a node agreement is in its life cycle. */
enum class NodeAgreementStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A node agreement as the node team keeps it. */
data class NodeAgreement(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var attempts: Int,
    var durationMinutes: Int,
    var budget: Double,
    var revision: Int,
    var status: NodeAgreementStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeAgreementStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeAgreementStatus): Boolean {
        when (status) {
            NodeAgreementStatus.PLANNED -> return next == NodeAgreementStatus.SCHEDULED || next == NodeAgreementStatus.COMPLETED
            NodeAgreementStatus.SCHEDULED -> return next == NodeAgreementStatus.COMPLETED || next == NodeAgreementStatus.CANCELLED
            NodeAgreementStatus.IN_PROGRESS -> {
                when (next) {
                    NodeAgreementStatus.COMPLETED, NodeAgreementStatus.CANCELLED -> return true
                }
                return false
            }
            NodeAgreementStatus.COMPLETED -> {
                when (next) {
                    NodeAgreementStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeAgreementStatus) {
        if (!canMoveTo(next)) {
            error("node agreement $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun netExposure(): Double = round((revision.toDouble() + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): NodeAgreement {
        return NodeAgreement(
            code = code,
            name = name,
            updatedAt = updatedAt,
            attempts = attempts,
            durationMinutes = durationMinutes,
            budget = budget,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a node agreement in status planned. */
fun newNodeAgreement(code: String, name: String): NodeAgreement {
    return NodeAgreement(
        code = code,
        name = name,
        updatedAt = 0,
        attempts = 0,
        durationMinutes = 0,
        budget = 0.0,
        revision = 0,
        status = NodeAgreementStatus.PLANNED,
    )
}
