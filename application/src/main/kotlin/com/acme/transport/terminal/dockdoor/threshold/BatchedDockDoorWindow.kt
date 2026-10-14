package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched dock door window is in its life cycle. */
enum class BatchedDockDoorWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a batched dock door window and its current status. */
data class BatchedDockDoorWindow(
    var code: String,
    var name: String,
    var rate: Double,
    var approved: Boolean,
    var trailerId: String,
    var active: Boolean,
    var priority: Int,
    var version: Int,
    var capacity: Int,
    var distanceKm: Double,
    var status: BatchedDockDoorWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDockDoorWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDockDoorWindowStatus): Boolean {
        when (status) {
            BatchedDockDoorWindowStatus.OPEN -> return next == BatchedDockDoorWindowStatus.ARCHIVED
            BatchedDockDoorWindowStatus.IN_REVIEW -> return next == BatchedDockDoorWindowStatus.RESOLVED || next == BatchedDockDoorWindowStatus.ARCHIVED
            BatchedDockDoorWindowStatus.RESOLVED -> {
                when (next) {
                    BatchedDockDoorWindowStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedDockDoorWindowStatus.REOPENED -> {
                when (next) {
                    BatchedDockDoorWindowStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedDockDoorWindowStatus) {
        if (!canMoveTo(next)) {
            error("batched dock door window $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun netValue(): Double = round(distanceKm * rate * 100) / 100

    fun snapshot(): BatchedDockDoorWindow {
        return BatchedDockDoorWindow(
            code = code,
            name = name,
            rate = rate,
            approved = approved,
            trailerId = trailerId,
            active = active,
            priority = priority,
            version = version,
            capacity = capacity,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched dock door window in status open. */
fun newBatchedDockDoorWindow(code: String, name: String): BatchedDockDoorWindow {
    return BatchedDockDoorWindow(
        code = code,
        name = name,
        rate = 0.0,
        approved = false,
        trailerId = "",
        active = false,
        priority = 0,
        version = 0,
        capacity = 0,
        distanceKm = 0.0,
        status = BatchedDockDoorWindowStatus.OPEN,
    )
}
