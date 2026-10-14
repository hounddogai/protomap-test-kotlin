package com.acme.transport.linehaul.capacity.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched capacity reservation records of linehaul's capacity work, and the rules, stores, and
// calculations around them.

/** Where a batched capacity reservation is in its life cycle. */
enum class BatchedCapacityReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One batched capacity reservation, with what the planners need to act on it. */
data class BatchedCapacityReservation(
    var code: String,
    var title: String,
    var threshold: Double,
    var segment: String,
    var carrier: String,
    var version: Int,
    var routeCode: String,
    var depot: String,
    var name: String,
    var status: BatchedCapacityReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCapacityReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCapacityReservationStatus): Boolean {
        when (status) {
            BatchedCapacityReservationStatus.PLANNED -> return next == BatchedCapacityReservationStatus.IN_PROGRESS
            BatchedCapacityReservationStatus.SCHEDULED -> return next == BatchedCapacityReservationStatus.IN_PROGRESS || next == BatchedCapacityReservationStatus.COMPLETED
            BatchedCapacityReservationStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedCapacityReservationStatus.COMPLETED, BatchedCapacityReservationStatus.CANCELLED -> return true
                }
                return false
            }
            BatchedCapacityReservationStatus.COMPLETED -> {
                when (next) {
                    BatchedCapacityReservationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedCapacityReservationStatus) {
        if (!canMoveTo(next)) {
            error("batched capacity reservation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun projectedLoad(): Double = round(threshold * version.toDouble() * 100) / 100

    fun snapshot(): BatchedCapacityReservation {
        return BatchedCapacityReservation(
            code = code,
            title = title,
            threshold = threshold,
            segment = segment,
            carrier = carrier,
            version = version,
            routeCode = routeCode,
            depot = depot,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a batched capacity reservation in status planned. */
fun newBatchedCapacityReservation(code: String, title: String): BatchedCapacityReservation {
    return BatchedCapacityReservation(
        code = code,
        title = title,
        threshold = 0.0,
        segment = "",
        carrier = "",
        version = 0,
        routeCode = "",
        depot = "",
        name = "",
        status = BatchedCapacityReservationStatus.PLANNED,
    )
}
