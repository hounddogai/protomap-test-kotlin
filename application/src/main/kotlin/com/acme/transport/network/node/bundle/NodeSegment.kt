package com.acme.transport.network.node.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node segment is in its life cycle. */
enum class NodeSegmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a node segment and its current status. */
data class NodeSegment(
    var code: String,
    var label: String,
    var priority: Int,
    var createdAt: Int,
    var budget: Double,
    var revision: Int,
    var retries: Int,
    var ratio: Double,
    var category: String,
    var status: NodeSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeSegmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeSegmentStatus): Boolean {
        when (status) {
            NodeSegmentStatus.PROPOSED -> return next == NodeSegmentStatus.WITHDRAWN
            NodeSegmentStatus.APPROVED -> return next == NodeSegmentStatus.REJECTED
            NodeSegmentStatus.REJECTED -> return next == NodeSegmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: NodeSegmentStatus) {
        if (!canMoveTo(next)) {
            error("node segment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun netLoad(): Double = round(budget * createdAt.toDouble() * 100) / 100

    fun snapshot(): NodeSegment {
        return NodeSegment(
            code = code,
            label = label,
            priority = priority,
            createdAt = createdAt,
            budget = budget,
            revision = revision,
            retries = retries,
            ratio = ratio,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a node segment in status proposed. */
fun newNodeSegment(code: String, label: String): NodeSegment {
    return NodeSegment(
        code = code,
        label = label,
        priority = 0,
        createdAt = 0,
        budget = 0.0,
        revision = 0,
        retries = 0,
        ratio = 0.0,
        category = "",
        status = NodeSegmentStatus.PROPOSED,
    )
}
