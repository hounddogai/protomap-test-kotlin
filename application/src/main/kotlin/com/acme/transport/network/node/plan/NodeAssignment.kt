package com.acme.transport.network.node.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a node assignment is in its life cycle. */
enum class NodeAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A node assignment as the node team keeps it. */
data class NodeAssignment(
    var code: String,
    var title: String,
    var visible: Boolean,
    var endsAt: Int,
    var createdAt: Int,
    var vehicleId: String,
    var windowDays: Int,
    var label: String,
    var status: NodeAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NodeAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: NodeAssignmentStatus): Boolean {
        when (status) {
            NodeAssignmentStatus.CREATED -> {
                when (next) {
                    NodeAssignmentStatus.RETIRED -> return true
                }
                return false
            }
            NodeAssignmentStatus.VALIDATED -> return next == NodeAssignmentStatus.PUBLISHED
            NodeAssignmentStatus.PUBLISHED -> return next == NodeAssignmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: NodeAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("node assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($title)"

    fun projectedTotal(): Double = round((windowDays.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): NodeAssignment {
        return NodeAssignment(
            code = code,
            title = title,
            visible = visible,
            endsAt = endsAt,
            createdAt = createdAt,
            vehicleId = vehicleId,
            windowDays = windowDays,
            label = label,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a node assignment in status created. */
fun newNodeAssignment(code: String, title: String): NodeAssignment {
    return NodeAssignment(
        code = code,
        title = title,
        visible = false,
        endsAt = 0,
        createdAt = 0,
        vehicleId = "",
        windowDays = 0,
        label = "",
        status = NodeAssignmentStatus.CREATED,
    )
}
