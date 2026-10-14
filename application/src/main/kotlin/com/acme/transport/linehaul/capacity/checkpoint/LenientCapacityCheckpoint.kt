package com.acme.transport.linehaul.capacity.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient capacity checkpoint records of linehaul's capacity work, and the rules, stores, and
// calculations around them.

/** Where a lenient capacity checkpoint is in its life cycle. */
enum class LenientCapacityCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A lenient capacity checkpoint. Its code is unique within a tenant. */
data class LenientCapacityCheckpoint(
    var code: String,
    var label: String,
    var ceiling: Double,
    var maximum: Double,
    var weight: Double,
    var quantity: Int,
    var margin: Double,
    var etaMinutes: Int,
    var status: LenientCapacityCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCapacityCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCapacityCheckpointStatus): Boolean {
        when (status) {
            LenientCapacityCheckpointStatus.IDLE -> return next == LenientCapacityCheckpointStatus.STOPPED
            LenientCapacityCheckpointStatus.WARMING -> return next == LenientCapacityCheckpointStatus.READY
            LenientCapacityCheckpointStatus.READY -> {
                when (next) {
                    LenientCapacityCheckpointStatus.DRAINING, LenientCapacityCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            LenientCapacityCheckpointStatus.DRAINING -> {
                when (next) {
                    LenientCapacityCheckpointStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientCapacityCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("lenient capacity checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netValue(): Double = round((etaMinutes.toDouble() - weight) * 100) / 100

    fun snapshot(): LenientCapacityCheckpoint {
        return LenientCapacityCheckpoint(
            code = code,
            label = label,
            ceiling = ceiling,
            maximum = maximum,
            weight = weight,
            quantity = quantity,
            margin = margin,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient capacity checkpoint in status idle. */
fun newLenientCapacityCheckpoint(code: String, label: String): LenientCapacityCheckpoint {
    return LenientCapacityCheckpoint(
        code = code,
        label = label,
        ceiling = 0.0,
        maximum = 0.0,
        weight = 0.0,
        quantity = 0,
        margin = 0.0,
        etaMinutes = 0,
        status = LenientCapacityCheckpointStatus.IDLE,
    )
}
