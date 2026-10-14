package com.acme.transport.network.node.bundle

import com.acme.transport.kit.text.normalize

/** Where a node proposal is in its life cycle. */
enum class NodeProposalStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A node proposal as the node team keeps it. */
data class NodeProposal(
    var code: String,
    var label: String,
    var description: String,
    var phone: String,
    var unit: String,
    var source: String,
    var loadKg: Double,
    var status: NodeProposalStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeProposalStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeProposalStatus): Boolean {
        when (status) {
            NodeProposalStatus.CREATED -> return next == NodeProposalStatus.VALIDATED || next == NodeProposalStatus.RETIRED
            NodeProposalStatus.VALIDATED -> {
                when (next) {
                    NodeProposalStatus.RETIRED -> return true
                }
                return false
            }
            NodeProposalStatus.PUBLISHED -> {
                when (next) {
                    NodeProposalStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeProposalStatus) {
        if (!canMoveTo(next)) {
            error("node proposal $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = loadKg

    fun snapshot(): NodeProposal {
        return NodeProposal(
            code = code,
            label = label,
            description = description,
            phone = phone,
            unit = unit,
            source = source,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a node proposal in status created. */
fun newNodeProposal(code: String, label: String): NodeProposal {
    return NodeProposal(
        code = code,
        label = label,
        description = "",
        phone = "",
        unit = "",
        source = "",
        loadKg = 0.0,
        status = NodeProposalStatus.CREATED,
    )
}
