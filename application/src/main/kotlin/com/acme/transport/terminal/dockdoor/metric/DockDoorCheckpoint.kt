package com.acme.transport.terminal.dockdoor.metric

import com.acme.transport.kit.text.normalize

/** Where a dock door checkpoint is in its life cycle. */
enum class DockDoorCheckpointStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One dock door checkpoint, with what the planners need to act on it. */
data class DockDoorCheckpoint(
    var code: String,
    var name: String,
    var trailerId: String,
    var active: Boolean,
    var budget: Double,
    var status: DockDoorCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorCheckpointStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorCheckpointStatus): Boolean {
        when (status) {
            DockDoorCheckpointStatus.PLANNED -> return next == DockDoorCheckpointStatus.COMPLETED || next == DockDoorCheckpointStatus.CANCELLED
            DockDoorCheckpointStatus.SCHEDULED -> return next == DockDoorCheckpointStatus.IN_PROGRESS
            DockDoorCheckpointStatus.IN_PROGRESS -> {
                when (next) {
                    DockDoorCheckpointStatus.COMPLETED -> return true
                }
                return false
            }
            DockDoorCheckpointStatus.COMPLETED -> return next == DockDoorCheckpointStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DockDoorCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("dock door checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = budget

    fun snapshot(): DockDoorCheckpoint {
        return DockDoorCheckpoint(
            code = code,
            name = name,
            trailerId = trailerId,
            active = active,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dock door checkpoint in status planned. */
fun newDockDoorCheckpoint(code: String, name: String): DockDoorCheckpoint {
    return DockDoorCheckpoint(
        code = code,
        name = name,
        trailerId = "",
        active = false,
        budget = 0.0,
        status = DockDoorCheckpointStatus.PLANNED,
    )
}
