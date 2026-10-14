package com.acme.transport.network.node.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node adjustment is in its life cycle. */
enum class NodeAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One node adjustment, with what the planners need to act on it. */
data class NodeAdjustment(
    var code: String,
    var title: String,
    var minimum: Double,
    var version: Int,
    var trailerId: String,
    var archived: Boolean,
    var etaMinutes: Int,
    var status: NodeAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeAdjustmentStatus): Boolean {
        when (status) {
            NodeAdjustmentStatus.NEW -> {
                when (next) {
                    NodeAdjustmentStatus.QUEUED, NodeAdjustmentStatus.DONE -> return true
                }
                return false
            }
            NodeAdjustmentStatus.QUEUED -> {
                when (next) {
                    NodeAdjustmentStatus.DONE -> return true
                }
                return false
            }
            NodeAdjustmentStatus.RUNNING -> {
                when (next) {
                    NodeAdjustmentStatus.DONE, NodeAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            NodeAdjustmentStatus.DONE -> {
                when (next) {
                    NodeAdjustmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("node adjustment $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"node adjustment"} $code"

    fun projectedLoad(): Double = round(minimum * version.toDouble() * 100) / 100

    fun snapshot(): NodeAdjustment {
        return NodeAdjustment(
            code = code,
            title = title,
            minimum = minimum,
            version = version,
            trailerId = trailerId,
            archived = archived,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a node adjustment in status new. */
fun newNodeAdjustment(code: String, title: String): NodeAdjustment {
    return NodeAdjustment(
        code = code,
        title = title,
        minimum = 0.0,
        version = 0,
        trailerId = "",
        archived = false,
        etaMinutes = 0,
        status = NodeAdjustmentStatus.NEW,
    )
}
