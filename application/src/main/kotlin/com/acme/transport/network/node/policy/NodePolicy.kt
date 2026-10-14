package com.acme.transport.network.node.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The node policy records of network's node work, and the rules, stores, and calculations around them.

/** Where a node policy is in its life cycle. */
enum class NodePolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One node policy, with what the planners need to act on it. */
data class NodePolicy(
    var code: String,
    var title: String,
    var stopCount: Int,
    var lane: String,
    var pallets: Int,
    var summary: String,
    var budget: Double,
    var locale: String,
    var status: NodePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodePolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodePolicyStatus): Boolean {
        when (status) {
            NodePolicyStatus.NEW -> return next == NodePolicyStatus.QUEUED
            NodePolicyStatus.QUEUED -> return next == NodePolicyStatus.RUNNING
            NodePolicyStatus.RUNNING -> {
                when (next) {
                    NodePolicyStatus.DONE -> return true
                }
                return false
            }
            NodePolicyStatus.DONE -> {
                when (next) {
                    NodePolicyStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodePolicyStatus) {
        if (!canMoveTo(next)) {
            error("node policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"node policy"} $code"

    fun effectiveValue(): Double = round((pallets.toDouble() + budget) * 100) / 100

    fun snapshot(): NodePolicy {
        return NodePolicy(
            code = code,
            title = title,
            stopCount = stopCount,
            lane = lane,
            pallets = pallets,
            summary = summary,
            budget = budget,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a node policy in status new. */
fun newNodePolicy(code: String, title: String): NodePolicy {
    return NodePolicy(
        code = code,
        title = title,
        stopCount = 0,
        lane = "",
        pallets = 0,
        summary = "",
        budget = 0.0,
        locale = "",
        status = NodePolicyStatus.NEW,
    )
}
