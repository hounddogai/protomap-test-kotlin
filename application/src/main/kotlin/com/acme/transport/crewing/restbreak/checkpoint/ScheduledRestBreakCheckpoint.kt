package com.acme.transport.crewing.restbreak.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled rest break checkpoint records of crewing's rest break work, and the rules, stores, and
// calculations around them.

/** Where a scheduled rest break checkpoint is in its life cycle. */
enum class ScheduledRestBreakCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A scheduled rest break checkpoint. Its code is unique within a tenant. */
data class ScheduledRestBreakCheckpoint(
    var code: String,
    var name: String,
    var margin: Double,
    var quantity: Int,
    var lane: String,
    var routeCode: String,
    var status: ScheduledRestBreakCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRestBreakCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRestBreakCheckpointStatus): Boolean {
        when (status) {
            ScheduledRestBreakCheckpointStatus.IDLE -> {
                when (next) {
                    ScheduledRestBreakCheckpointStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledRestBreakCheckpointStatus.WARMING -> return next == ScheduledRestBreakCheckpointStatus.READY || next == ScheduledRestBreakCheckpointStatus.DRAINING
            ScheduledRestBreakCheckpointStatus.READY -> {
                when (next) {
                    ScheduledRestBreakCheckpointStatus.DRAINING, ScheduledRestBreakCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledRestBreakCheckpointStatus.DRAINING -> return next == ScheduledRestBreakCheckpointStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledRestBreakCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("scheduled rest break checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveTotal(): Double = round((quantity.toDouble() + margin) * 100) / 100

    fun snapshot(): ScheduledRestBreakCheckpoint {
        return ScheduledRestBreakCheckpoint(
            code = code,
            name = name,
            margin = margin,
            quantity = quantity,
            lane = lane,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled rest break checkpoint in status idle. */
fun newScheduledRestBreakCheckpoint(code: String, name: String): ScheduledRestBreakCheckpoint {
    return ScheduledRestBreakCheckpoint(
        code = code,
        name = name,
        margin = 0.0,
        quantity = 0,
        lane = "",
        routeCode = "",
        status = ScheduledRestBreakCheckpointStatus.IDLE,
    )
}
