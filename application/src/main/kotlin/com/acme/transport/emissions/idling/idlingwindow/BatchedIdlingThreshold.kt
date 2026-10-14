package com.acme.transport.emissions.idling.idlingwindow

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched idling threshold is in its life cycle. */
enum class BatchedIdlingThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One batched idling threshold, with what the planners need to act on it. */
data class BatchedIdlingThreshold(
    var code: String,
    var name: String,
    var contactEmail: String,
    var lane: String,
    var batchSize: Int,
    var externalRef: String,
    var notes: String,
    var fuelLiters: Double,
    var depot: String,
    var status: BatchedIdlingThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedIdlingThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedIdlingThresholdStatus): Boolean {
        when (status) {
            BatchedIdlingThresholdStatus.PLANNED -> {
                when (next) {
                    BatchedIdlingThresholdStatus.IN_PROGRESS -> return true
                }
                return false
            }
            BatchedIdlingThresholdStatus.SCHEDULED -> {
                when (next) {
                    BatchedIdlingThresholdStatus.COMPLETED, BatchedIdlingThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedIdlingThresholdStatus.IN_PROGRESS -> return next == BatchedIdlingThresholdStatus.COMPLETED || next == BatchedIdlingThresholdStatus.CANCELLED
            BatchedIdlingThresholdStatus.COMPLETED -> return next == BatchedIdlingThresholdStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BatchedIdlingThresholdStatus) {
        if (!canMoveTo(next)) {
            error("batched idling threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netTotal(): Double = round((fuelLiters - batchSize.toDouble()) * 100) / 100

    fun snapshot(): BatchedIdlingThreshold {
        return BatchedIdlingThreshold(
            code = code,
            name = name,
            contactEmail = contactEmail,
            lane = lane,
            batchSize = batchSize,
            externalRef = externalRef,
            notes = notes,
            fuelLiters = fuelLiters,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched idling threshold in status planned. */
fun newBatchedIdlingThreshold(code: String, name: String): BatchedIdlingThreshold {
    return BatchedIdlingThreshold(
        code = code,
        name = name,
        contactEmail = "",
        lane = "",
        batchSize = 0,
        externalRef = "",
        notes = "",
        fuelLiters = 0.0,
        depot = "",
        status = BatchedIdlingThresholdStatus.PLANNED,
    )
}
