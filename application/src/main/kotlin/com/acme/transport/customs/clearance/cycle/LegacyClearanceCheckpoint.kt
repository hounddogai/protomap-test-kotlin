package com.acme.transport.customs.clearance.cycle

import com.acme.transport.kit.text.normalize

/** Where a legacy clearance checkpoint is in its life cycle. */
enum class LegacyClearanceCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy clearance checkpoint as the clearance team keeps it. */
data class LegacyClearanceCheckpoint(
    var code: String,
    var name: String,
    var ownerId: String,
    var label: String,
    var summary: String,
    var cost: Double,
    var status: LegacyClearanceCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyClearanceCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyClearanceCheckpointStatus): Boolean {
        when (status) {
            LegacyClearanceCheckpointStatus.IDLE -> {
                when (next) {
                    LegacyClearanceCheckpointStatus.WARMING, LegacyClearanceCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            LegacyClearanceCheckpointStatus.WARMING -> {
                when (next) {
                    LegacyClearanceCheckpointStatus.DRAINING, LegacyClearanceCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            LegacyClearanceCheckpointStatus.READY -> {
                when (next) {
                    LegacyClearanceCheckpointStatus.DRAINING, LegacyClearanceCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            LegacyClearanceCheckpointStatus.DRAINING -> return next == LegacyClearanceCheckpointStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: LegacyClearanceCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("legacy clearance checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $name"

    fun measuredValue(): Double = cost

    fun snapshot(): LegacyClearanceCheckpoint {
        return LegacyClearanceCheckpoint(
            code = code,
            name = name,
            ownerId = ownerId,
            label = label,
            summary = summary,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a legacy clearance checkpoint in status idle. */
fun newLegacyClearanceCheckpoint(code: String, name: String): LegacyClearanceCheckpoint {
    return LegacyClearanceCheckpoint(
        code = code,
        name = name,
        ownerId = "",
        label = "",
        summary = "",
        cost = 0.0,
        status = LegacyClearanceCheckpointStatus.IDLE,
    )
}
