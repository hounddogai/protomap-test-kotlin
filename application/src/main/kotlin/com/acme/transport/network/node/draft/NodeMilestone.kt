package com.acme.transport.network.node.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node milestone is in its life cycle. */
enum class NodeMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a node milestone and its current status. */
data class NodeMilestone(
    var code: String,
    var label: String,
    var segment: String,
    var trailerId: String,
    var region: String,
    var approved: Boolean,
    var version: Int,
    var reviewer: String,
    var score: Double,
    var threshold: Double,
    var status: NodeMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeMilestoneStatus): Boolean {
        when (status) {
            NodeMilestoneStatus.DRAFT -> {
                when (next) {
                    NodeMilestoneStatus.ACTIVE -> return true
                }
                return false
            }
            NodeMilestoneStatus.PENDING -> return next == NodeMilestoneStatus.ACTIVE || next == NodeMilestoneStatus.SUSPENDED
            NodeMilestoneStatus.ACTIVE -> {
                when (next) {
                    NodeMilestoneStatus.SUSPENDED, NodeMilestoneStatus.CLOSED -> return true
                }
                return false
            }
            NodeMilestoneStatus.SUSPENDED -> return next == NodeMilestoneStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: NodeMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("node milestone $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun netVolume(): Double = round((threshold - score) * 100) / 100

    fun snapshot(): NodeMilestone {
        return NodeMilestone(
            code = code,
            label = label,
            segment = segment,
            trailerId = trailerId,
            region = region,
            approved = approved,
            version = version,
            reviewer = reviewer,
            score = score,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a node milestone in status draft. */
fun newNodeMilestone(code: String, label: String): NodeMilestone {
    return NodeMilestone(
        code = code,
        label = label,
        segment = "",
        trailerId = "",
        region = "",
        approved = false,
        version = 0,
        reviewer = "",
        score = 0.0,
        threshold = 0.0,
        status = NodeMilestoneStatus.DRAFT,
    )
}
