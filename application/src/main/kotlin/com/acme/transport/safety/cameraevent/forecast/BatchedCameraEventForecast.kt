package com.acme.transport.safety.cameraevent.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched camera event forecast records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a batched camera event forecast is in its life cycle. */
enum class BatchedCameraEventForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a batched camera event forecast and its current status. */
data class BatchedCameraEventForecast(
    var code: String,
    var name: String,
    var stopCount: Int,
    var sequence: Int,
    var source: String,
    var reviewer: String,
    var startsAt: Int,
    var status: BatchedCameraEventForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCameraEventForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCameraEventForecastStatus): Boolean {
        when (status) {
            BatchedCameraEventForecastStatus.PLANNED -> return next == BatchedCameraEventForecastStatus.SCHEDULED || next == BatchedCameraEventForecastStatus.IN_PROGRESS
            BatchedCameraEventForecastStatus.SCHEDULED -> {
                when (next) {
                    BatchedCameraEventForecastStatus.IN_PROGRESS -> return true
                }
                return false
            }
            BatchedCameraEventForecastStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedCameraEventForecastStatus.COMPLETED, BatchedCameraEventForecastStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedCameraEventForecastStatus.COMPLETED -> {
                when (next) {
                    BatchedCameraEventForecastStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedCameraEventForecastStatus) {
        if (!canMoveTo(next)) {
            error("batched camera event forecast $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun projectedTotal(): Double = round((startsAt.toDouble() - sequence.toDouble()) * 100) / 100

    fun snapshot(): BatchedCameraEventForecast {
        return BatchedCameraEventForecast(
            code = code,
            name = name,
            stopCount = stopCount,
            sequence = sequence,
            source = source,
            reviewer = reviewer,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched camera event forecast in status planned. */
fun newBatchedCameraEventForecast(code: String, name: String): BatchedCameraEventForecast {
    return BatchedCameraEventForecast(
        code = code,
        name = name,
        stopCount = 0,
        sequence = 0,
        source = "",
        reviewer = "",
        startsAt = 0,
        status = BatchedCameraEventForecastStatus.PLANNED,
    )
}
