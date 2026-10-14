package com.acme.transport.network.node.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node audit is in its life cycle. */
enum class NodeAuditStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a node audit and its current status. */
data class NodeAudit(
    var code: String,
    var label: String,
    var ownerId: String,
    var capacity: Int,
    var notes: String,
    var loadKg: Double,
    var weight: Double,
    var segment: String,
    var summary: String,
    var status: NodeAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeAuditStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeAuditStatus): Boolean {
        when (status) {
            NodeAuditStatus.PLANNED -> return next == NodeAuditStatus.IN_PROGRESS
            NodeAuditStatus.SCHEDULED -> {
                when (next) {
                    NodeAuditStatus.COMPLETED, NodeAuditStatus.CANCELLED -> return true
                }
                return false
            }
            NodeAuditStatus.IN_PROGRESS -> return next == NodeAuditStatus.CANCELLED
            NodeAuditStatus.COMPLETED -> {
                when (next) {
                    NodeAuditStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeAuditStatus) {
        if (!canMoveTo(next)) {
            error("node audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"node audit"} $code"

    fun effectiveExposure(): Double = round(loadKg * capacity.toDouble() * 100) / 100

    fun snapshot(): NodeAudit {
        return NodeAudit(
            code = code,
            label = label,
            ownerId = ownerId,
            capacity = capacity,
            notes = notes,
            loadKg = loadKg,
            weight = weight,
            segment = segment,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a node audit in status planned. */
fun newNodeAudit(code: String, label: String): NodeAudit {
    return NodeAudit(
        code = code,
        label = label,
        ownerId = "",
        capacity = 0,
        notes = "",
        loadKg = 0.0,
        weight = 0.0,
        segment = "",
        summary = "",
        status = NodeAuditStatus.PLANNED,
    )
}
