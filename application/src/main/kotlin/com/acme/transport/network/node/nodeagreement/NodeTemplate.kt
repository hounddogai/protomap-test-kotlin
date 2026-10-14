package com.acme.transport.network.node.nodeagreement

import com.acme.transport.kit.text.normalize

/** Where a node template is in its life cycle. */
enum class NodeTemplateStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a node template and its current status. */
data class NodeTemplate(
    var code: String,
    var name: String,
    var checksum: String,
    var notes: String,
    var carrier: String,
    var country: String,
    var quantity: Int,
    var status: NodeTemplateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeTemplateStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeTemplateStatus): Boolean {
        when (status) {
            NodeTemplateStatus.PLANNED -> return next == NodeTemplateStatus.IN_PROGRESS || next == NodeTemplateStatus.CANCELLED
            NodeTemplateStatus.SCHEDULED -> return next == NodeTemplateStatus.COMPLETED
            NodeTemplateStatus.IN_PROGRESS -> {
                when (next) {
                    NodeTemplateStatus.COMPLETED, NodeTemplateStatus.CANCELLED -> return true
                }
                return false
            }
            NodeTemplateStatus.COMPLETED -> {
                when (next) {
                    NodeTemplateStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeTemplateStatus) {
        if (!canMoveTo(next)) {
            error("node template $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): NodeTemplate {
        return NodeTemplate(
            code = code,
            name = name,
            checksum = checksum,
            notes = notes,
            carrier = carrier,
            country = country,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a node template in status planned. */
fun newNodeTemplate(code: String, name: String): NodeTemplate {
    return NodeTemplate(
        code = code,
        name = name,
        checksum = "",
        notes = "",
        carrier = "",
        country = "",
        quantity = 0,
        status = NodeTemplateStatus.PLANNED,
    )
}
