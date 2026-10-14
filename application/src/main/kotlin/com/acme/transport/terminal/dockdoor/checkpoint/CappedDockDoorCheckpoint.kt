package com.acme.transport.terminal.dockdoor.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped dock door checkpoint records of terminal's dock door work, and the rules, stores, and
// calculations around them.

/** Where a capped dock door checkpoint is in its life cycle. */
enum class CappedDockDoorCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a capped dock door checkpoint and its current status. */
data class CappedDockDoorCheckpoint(
    var code: String,
    var title: String,
    var label: String,
    var stopCount: Int,
    var floorValue: Double,
    var status: CappedDockDoorCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDockDoorCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDockDoorCheckpointStatus): Boolean {
        when (status) {
            CappedDockDoorCheckpointStatus.CREATED -> return next == CappedDockDoorCheckpointStatus.RETIRED
            CappedDockDoorCheckpointStatus.VALIDATED -> return next == CappedDockDoorCheckpointStatus.PUBLISHED || next == CappedDockDoorCheckpointStatus.RETIRED
            CappedDockDoorCheckpointStatus.PUBLISHED -> {
                when (next) {
                    CappedDockDoorCheckpointStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedDockDoorCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("capped dock door checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $title"

    fun adjustedLoad(): Double = round((stopCount.toDouble() - floorValue) * 100) / 100

    fun snapshot(): CappedDockDoorCheckpoint {
        return CappedDockDoorCheckpoint(
            code = code,
            title = title,
            label = label,
            stopCount = stopCount,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped dock door checkpoint in status created. */
fun newCappedDockDoorCheckpoint(code: String, title: String): CappedDockDoorCheckpoint {
    return CappedDockDoorCheckpoint(
        code = code,
        title = title,
        label = "",
        stopCount = 0,
        floorValue = 0.0,
        status = CappedDockDoorCheckpointStatus.CREATED,
    )
}
