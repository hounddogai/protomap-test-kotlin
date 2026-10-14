package com.acme.transport.network.node.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node rollup is in its life cycle. */
enum class NodeRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One node rollup, with what the planners need to act on it. */
data class NodeRollup(
    var code: String,
    var label: String,
    var priority: Int,
    var dock: String,
    var margin: Double,
    var status: NodeRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeRollupStatus): Boolean {
        when (status) {
            NodeRollupStatus.OPEN -> return next == NodeRollupStatus.RESOLVED || next == NodeRollupStatus.ARCHIVED
            NodeRollupStatus.IN_REVIEW -> return next == NodeRollupStatus.ARCHIVED
            NodeRollupStatus.RESOLVED -> return next == NodeRollupStatus.ARCHIVED
            NodeRollupStatus.REOPENED -> return next == NodeRollupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: NodeRollupStatus) {
        if (!canMoveTo(next)) {
            error("node rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"node rollup"} $code"

    fun projectedLoad(): Double = round((priority.toDouble() - margin) * 100) / 100

    fun snapshot(): NodeRollup {
        return NodeRollup(
            code = code,
            label = label,
            priority = priority,
            dock = dock,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a node rollup in status open. */
fun newNodeRollup(code: String, label: String): NodeRollup {
    return NodeRollup(
        code = code,
        label = label,
        priority = 0,
        dock = "",
        margin = 0.0,
        status = NodeRollupStatus.OPEN,
    )
}
