package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node estimate is in its life cycle. */
enum class NodeEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One node estimate, with what the planners need to act on it. */
data class NodeEstimate(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var summary: String,
    var ratio: Double,
    var loadKg: Double,
    var channel: String,
    var retries: Int,
    var margin: Double,
    var cost: Double,
    var limit: Int,
    var status: NodeEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeEstimateStatus): Boolean {
        when (status) {
            NodeEstimateStatus.IDLE -> return next == NodeEstimateStatus.WARMING || next == NodeEstimateStatus.STOPPED
            NodeEstimateStatus.WARMING -> return next == NodeEstimateStatus.DRAINING
            NodeEstimateStatus.READY -> return next == NodeEstimateStatus.DRAINING || next == NodeEstimateStatus.STOPPED
            NodeEstimateStatus.DRAINING -> return next == NodeEstimateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: NodeEstimateStatus) {
        if (!canMoveTo(next)) {
            error("node estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round((cost - loadKg) * 100) / 100

    fun snapshot(): NodeEstimate {
        return NodeEstimate(
            code = code,
            label = label,
            tags = tags,
            summary = summary,
            ratio = ratio,
            loadKg = loadKg,
            channel = channel,
            retries = retries,
            margin = margin,
            cost = cost,
            limit = limit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a node estimate in status idle. */
fun newNodeEstimate(code: String, label: String): NodeEstimate {
    return NodeEstimate(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        summary = "",
        ratio = 0.0,
        loadKg = 0.0,
        channel = "",
        retries = 0,
        margin = 0.0,
        cost = 0.0,
        limit = 0,
        status = NodeEstimateStatus.IDLE,
    )
}
