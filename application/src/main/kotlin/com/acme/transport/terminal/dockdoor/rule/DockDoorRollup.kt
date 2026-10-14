package com.acme.transport.terminal.dockdoor.rule

import com.acme.transport.kit.text.normalize

/** Where a dock door rollup is in its life cycle. */
enum class DockDoorRollupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One dock door rollup, with what the planners need to act on it. */
data class DockDoorRollup(
    var code: String,
    var label: String,
    var carrier: String,
    var active: Boolean,
    var trailerId: String,
    var ownerId: String,
    var depot: String,
    var score: Double,
    var status: DockDoorRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorRollupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorRollupStatus): Boolean {
        when (status) {
            DockDoorRollupStatus.PLANNED -> return next == DockDoorRollupStatus.CANCELLED
            DockDoorRollupStatus.SCHEDULED -> {
                when (next) {
                    DockDoorRollupStatus.COMPLETED -> return true
                }
                return false
            }
            DockDoorRollupStatus.IN_PROGRESS -> {
                when (next) {
                    DockDoorRollupStatus.COMPLETED -> return true
                }
                return false
            }
            DockDoorRollupStatus.COMPLETED -> return next == DockDoorRollupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: DockDoorRollupStatus) {
        if (!canMoveTo(next)) {
            error("dock door rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"dock door rollup"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): DockDoorRollup {
        return DockDoorRollup(
            code = code,
            label = label,
            carrier = carrier,
            active = active,
            trailerId = trailerId,
            ownerId = ownerId,
            depot = depot,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a dock door rollup in status planned. */
fun newDockDoorRollup(code: String, label: String): DockDoorRollup {
    return DockDoorRollup(
        code = code,
        label = label,
        carrier = "",
        active = false,
        trailerId = "",
        ownerId = "",
        depot = "",
        score = 0.0,
        status = DockDoorRollupStatus.PLANNED,
    )
}
