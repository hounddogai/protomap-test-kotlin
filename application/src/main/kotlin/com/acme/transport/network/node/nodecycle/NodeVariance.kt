package com.acme.transport.network.node.nodecycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node variance is in its life cycle. */
enum class NodeVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a node variance and its current status. */
data class NodeVariance(
    var code: String,
    var name: String,
    var threshold: Double,
    var createdAt: Int,
    var discount: Double,
    var endsAt: Int,
    var quantity: Int,
    var ceiling: Double,
    var amount: Double,
    var contactEmail: String,
    var status: NodeVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeVarianceStatus): Boolean {
        when (status) {
            NodeVarianceStatus.PROPOSED -> return next == NodeVarianceStatus.REJECTED || next == NodeVarianceStatus.WITHDRAWN
            NodeVarianceStatus.APPROVED -> return next == NodeVarianceStatus.REJECTED
            NodeVarianceStatus.REJECTED -> return next == NodeVarianceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: NodeVarianceStatus) {
        if (!canMoveTo(next)) {
            error("node variance $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun adjustedTotal(): Double = round((ceiling - endsAt.toDouble()) * 100) / 100

    fun snapshot(): NodeVariance {
        return NodeVariance(
            code = code,
            name = name,
            threshold = threshold,
            createdAt = createdAt,
            discount = discount,
            endsAt = endsAt,
            quantity = quantity,
            ceiling = ceiling,
            amount = amount,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a node variance in status proposed. */
fun newNodeVariance(code: String, name: String): NodeVariance {
    return NodeVariance(
        code = code,
        name = name,
        threshold = 0.0,
        createdAt = 0,
        discount = 0.0,
        endsAt = 0,
        quantity = 0,
        ceiling = 0.0,
        amount = 0.0,
        contactEmail = "",
        status = NodeVarianceStatus.PROPOSED,
    )
}
