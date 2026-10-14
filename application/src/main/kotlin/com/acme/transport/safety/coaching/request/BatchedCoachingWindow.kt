package com.acme.transport.safety.coaching.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched coaching window is in its life cycle. */
enum class BatchedCoachingWindowStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One batched coaching window, with what the planners need to act on it. */
data class BatchedCoachingWindow(
    var code: String,
    var label: String,
    var channel: String,
    var odometer: Int,
    var minimum: Double,
    var distanceKm: Double,
    var status: BatchedCoachingWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCoachingWindowStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCoachingWindowStatus): Boolean {
        when (status) {
            BatchedCoachingWindowStatus.IDLE -> return next == BatchedCoachingWindowStatus.DRAINING
            BatchedCoachingWindowStatus.WARMING -> return next == BatchedCoachingWindowStatus.READY || next == BatchedCoachingWindowStatus.DRAINING
            BatchedCoachingWindowStatus.READY -> return next == BatchedCoachingWindowStatus.DRAINING
            BatchedCoachingWindowStatus.DRAINING -> {
                when (next) {
                    BatchedCoachingWindowStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedCoachingWindowStatus) {
        if (!canMoveTo(next)) {
            error("batched coaching window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedValue(): Double = round((minimum - odometer.toDouble()) * 100) / 100

    fun snapshot(): BatchedCoachingWindow {
        return BatchedCoachingWindow(
            code = code,
            label = label,
            channel = channel,
            odometer = odometer,
            minimum = minimum,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a batched coaching window in status idle. */
fun newBatchedCoachingWindow(code: String, label: String): BatchedCoachingWindow {
    return BatchedCoachingWindow(
        code = code,
        label = label,
        channel = "",
        odometer = 0,
        minimum = 0.0,
        distanceKm = 0.0,
        status = BatchedCoachingWindowStatus.IDLE,
    )
}
