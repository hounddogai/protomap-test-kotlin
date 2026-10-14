package com.acme.transport.network.node.forecast

import com.acme.transport.kit.text.normalize

/** Where a node ledger is in its life cycle. */
enum class NodeLedgerStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A node ledger. Its code is unique within a tenant. */
data class NodeLedger(
    var code: String,
    var label: String,
    var locale: String,
    var etaMinutes: Int,
    var notes: String,
    var status: NodeLedgerStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeLedgerStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeLedgerStatus): Boolean {
        when (status) {
            NodeLedgerStatus.IDLE -> return next == NodeLedgerStatus.WARMING || next == NodeLedgerStatus.READY
            NodeLedgerStatus.WARMING -> {
                when (next) {
                    NodeLedgerStatus.READY, NodeLedgerStatus.DRAINING -> return true
                }
                return false
            }
            NodeLedgerStatus.READY -> return next == NodeLedgerStatus.DRAINING || next == NodeLedgerStatus.STOPPED
            NodeLedgerStatus.DRAINING -> return next == NodeLedgerStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: NodeLedgerStatus) {
        if (!canMoveTo(next)) {
            error("node ledger $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"node ledger"} $code"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): NodeLedger {
        return NodeLedger(
            code = code,
            label = label,
            locale = locale,
            etaMinutes = etaMinutes,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a node ledger in status idle. */
fun newNodeLedger(code: String, label: String): NodeLedger {
    return NodeLedger(
        code = code,
        label = label,
        locale = "",
        etaMinutes = 0,
        notes = "",
        status = NodeLedgerStatus.IDLE,
    )
}
