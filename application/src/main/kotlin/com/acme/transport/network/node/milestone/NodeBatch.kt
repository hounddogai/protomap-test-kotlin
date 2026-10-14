package com.acme.transport.network.node.milestone

import com.acme.transport.kit.text.normalize

/** Where a node batch is in its life cycle. */
enum class NodeBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One node batch, with what the planners need to act on it. */
data class NodeBatch(
    var code: String,
    var title: String,
    var active: Boolean,
    var source: String,
    var carrier: String,
    var timezone: String,
    var externalRef: String,
    var depot: String,
    var maximum: Double,
    var currency: String,
    var status: NodeBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeBatchStatus): Boolean {
        when (status) {
            NodeBatchStatus.NEW -> return next == NodeBatchStatus.RUNNING || next == NodeBatchStatus.FAILED
            NodeBatchStatus.QUEUED -> return next == NodeBatchStatus.RUNNING
            NodeBatchStatus.RUNNING -> return next == NodeBatchStatus.FAILED
            NodeBatchStatus.DONE -> return next == NodeBatchStatus.FAILED
        }
        return false
    }

    fun moveTo(next: NodeBatchStatus) {
        if (!canMoveTo(next)) {
            error("node batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = maximum

    fun snapshot(): NodeBatch {
        return NodeBatch(
            code = code,
            title = title,
            active = active,
            source = source,
            carrier = carrier,
            timezone = timezone,
            externalRef = externalRef,
            depot = depot,
            maximum = maximum,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a node batch in status new. */
fun newNodeBatch(code: String, title: String): NodeBatch {
    return NodeBatch(
        code = code,
        title = title,
        active = false,
        source = "",
        carrier = "",
        timezone = "",
        externalRef = "",
        depot = "",
        maximum = 0.0,
        currency = "",
        status = NodeBatchStatus.NEW,
    )
}
