package com.acme.transport.network.node.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node tier is in its life cycle. */
enum class NodeTierStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One node tier, with what the planners need to act on it. */
data class NodeTier(
    var code: String,
    var label: String,
    var margin: Double,
    var maximum: Double,
    var revision: Int,
    var minimum: Double,
    var name: String,
    var title: String,
    var status: NodeTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeTierStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeTierStatus): Boolean {
        when (status) {
            NodeTierStatus.REQUESTED -> return next == NodeTierStatus.GRANTED || next == NodeTierStatus.EXPIRED
            NodeTierStatus.GRANTED -> return next == NodeTierStatus.REVOKED || next == NodeTierStatus.EXPIRED
            NodeTierStatus.REVOKED -> return next == NodeTierStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: NodeTierStatus) {
        if (!canMoveTo(next)) {
            error("node tier $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedVolume(): Double = round((revision.toDouble() - minimum) * 100) / 100

    fun snapshot(): NodeTier {
        return NodeTier(
            code = code,
            label = label,
            margin = margin,
            maximum = maximum,
            revision = revision,
            minimum = minimum,
            name = name,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a node tier in status requested. */
fun newNodeTier(code: String, label: String): NodeTier {
    return NodeTier(
        code = code,
        label = label,
        margin = 0.0,
        maximum = 0.0,
        revision = 0,
        minimum = 0.0,
        name = "",
        title = "",
        status = NodeTierStatus.REQUESTED,
    )
}
