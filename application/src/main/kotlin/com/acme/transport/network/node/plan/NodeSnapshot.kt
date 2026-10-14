package com.acme.transport.network.node.plan

import com.acme.transport.kit.text.normalize

/** Where a node snapshot is in its life cycle. */
enum class NodeSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A node snapshot as the node team keeps it. */
data class NodeSnapshot(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var shiftCode: String,
    var checksum: String,
    var pallets: Int,
    var status: NodeSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeSnapshotStatus): Boolean {
        when (status) {
            NodeSnapshotStatus.PLANNED -> {
                when (next) {
                    NodeSnapshotStatus.IN_PROGRESS, NodeSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
            NodeSnapshotStatus.SCHEDULED -> return next == NodeSnapshotStatus.IN_PROGRESS || next == NodeSnapshotStatus.CANCELLED
            NodeSnapshotStatus.IN_PROGRESS -> return next == NodeSnapshotStatus.COMPLETED
            NodeSnapshotStatus.COMPLETED -> {
                when (next) {
                    NodeSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: NodeSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("node snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): NodeSnapshot {
        return NodeSnapshot(
            code = code,
            label = label,
            pinned = pinned,
            shiftCode = shiftCode,
            checksum = checksum,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a node snapshot in status planned. */
fun newNodeSnapshot(code: String, label: String): NodeSnapshot {
    return NodeSnapshot(
        code = code,
        label = label,
        pinned = false,
        shiftCode = "",
        checksum = "",
        pallets = 0,
        status = NodeSnapshotStatus.PLANNED,
    )
}
