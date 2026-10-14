package com.acme.transport.terminal.dockdoor.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched dock door baseline is in its life cycle. */
enum class BatchedDockDoorBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched dock door baseline as the dock door team keeps it. */
data class BatchedDockDoorBaseline(
    var code: String,
    var title: String,
    var carrier: String,
    var score: Double,
    var rate: Double,
    var distanceKm: Double,
    var capacity: Int,
    var status: BatchedDockDoorBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDockDoorBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDockDoorBaselineStatus): Boolean {
        when (status) {
            BatchedDockDoorBaselineStatus.IDLE -> {
                when (next) {
                    BatchedDockDoorBaselineStatus.READY, BatchedDockDoorBaselineStatus.STOPPED -> return true
                }
                return false
            }
            BatchedDockDoorBaselineStatus.WARMING -> return next == BatchedDockDoorBaselineStatus.READY || next == BatchedDockDoorBaselineStatus.STOPPED
            BatchedDockDoorBaselineStatus.READY -> return next == BatchedDockDoorBaselineStatus.DRAINING
            BatchedDockDoorBaselineStatus.DRAINING -> return next == BatchedDockDoorBaselineStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedDockDoorBaselineStatus) {
        if (!canMoveTo(next)) {
            error("batched dock door baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedVolume(): Double = round((score - rate) * 100) / 100

    fun snapshot(): BatchedDockDoorBaseline {
        return BatchedDockDoorBaseline(
            code = code,
            title = title,
            carrier = carrier,
            score = score,
            rate = rate,
            distanceKm = distanceKm,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched dock door baseline in status idle. */
fun newBatchedDockDoorBaseline(code: String, title: String): BatchedDockDoorBaseline {
    return BatchedDockDoorBaseline(
        code = code,
        title = title,
        carrier = "",
        score = 0.0,
        rate = 0.0,
        distanceKm = 0.0,
        capacity = 0,
        status = BatchedDockDoorBaselineStatus.IDLE,
    )
}
