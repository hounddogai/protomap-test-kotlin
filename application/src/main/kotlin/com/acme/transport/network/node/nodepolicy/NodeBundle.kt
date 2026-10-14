package com.acme.transport.network.node.nodepolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node bundle is in its life cycle. */
enum class NodeBundleStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a node bundle and its current status. */
data class NodeBundle(
    var code: String,
    var label: String,
    var shiftCode: String,
    var revision: Int,
    var reviewer: String,
    var visible: Boolean,
    var pallets: Int,
    var cost: Double,
    var startsAt: Int,
    var summary: String,
    var priority: Int,
    var status: NodeBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeBundleStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeBundleStatus): Boolean {
        when (status) {
            NodeBundleStatus.PLANNED -> return next == NodeBundleStatus.COMPLETED
            NodeBundleStatus.SCHEDULED -> return next == NodeBundleStatus.IN_PROGRESS || next == NodeBundleStatus.CANCELLED
            NodeBundleStatus.IN_PROGRESS -> {
                when (next) {
                    NodeBundleStatus.COMPLETED -> return true
                }
                return false
            }
            NodeBundleStatus.COMPLETED -> {
                when (next) {
                    NodeBundleStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeBundleStatus) {
        if (!canMoveTo(next)) {
            error("node bundle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedVolume(): Double = round((startsAt.toDouble() + cost) * 100) / 100

    fun snapshot(): NodeBundle {
        return NodeBundle(
            code = code,
            label = label,
            shiftCode = shiftCode,
            revision = revision,
            reviewer = reviewer,
            visible = visible,
            pallets = pallets,
            cost = cost,
            startsAt = startsAt,
            summary = summary,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a node bundle in status planned. */
fun newNodeBundle(code: String, label: String): NodeBundle {
    return NodeBundle(
        code = code,
        label = label,
        shiftCode = "",
        revision = 0,
        reviewer = "",
        visible = false,
        pallets = 0,
        cost = 0.0,
        startsAt = 0,
        summary = "",
        priority = 0,
        status = NodeBundleStatus.PLANNED,
    )
}
