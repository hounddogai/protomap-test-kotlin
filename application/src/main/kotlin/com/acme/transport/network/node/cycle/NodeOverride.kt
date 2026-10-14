package com.acme.transport.network.node.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node override is in its life cycle. */
enum class NodeOverrideStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One node override, with what the planners need to act on it. */
data class NodeOverride(
    var code: String,
    var title: String,
    var score: Double,
    var retries: Int,
    var windowDays: Int,
    var carrier: String,
    var cost: Double,
    var createdAt: Int,
    var dock: String,
    var version: Int,
    var active: Boolean,
    var status: NodeOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeOverrideStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeOverrideStatus): Boolean {
        when (status) {
            NodeOverrideStatus.PROPOSED -> {
                when (next) {
                    NodeOverrideStatus.APPROVED -> return true
                }
                return false
            }
            NodeOverrideStatus.APPROVED -> {
                when (next) {
                    NodeOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
            NodeOverrideStatus.REJECTED -> return next == NodeOverrideStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: NodeOverrideStatus) {
        if (!canMoveTo(next)) {
            error("node override $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "${"node override"} $code"

    fun adjustedVolume(): Double = round(version.toDouble() * cost * 100) / 100

    fun snapshot(): NodeOverride {
        return NodeOverride(
            code = code,
            title = title,
            score = score,
            retries = retries,
            windowDays = windowDays,
            carrier = carrier,
            cost = cost,
            createdAt = createdAt,
            dock = dock,
            version = version,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a node override in status proposed. */
fun newNodeOverride(code: String, title: String): NodeOverride {
    return NodeOverride(
        code = code,
        title = title,
        score = 0.0,
        retries = 0,
        windowDays = 0,
        carrier = "",
        cost = 0.0,
        createdAt = 0,
        dock = "",
        version = 0,
        active = false,
        status = NodeOverrideStatus.PROPOSED,
    )
}
