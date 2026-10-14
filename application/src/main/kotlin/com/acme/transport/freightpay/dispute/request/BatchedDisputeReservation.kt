package com.acme.transport.freightpay.dispute.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched dispute reservation is in its life cycle. */
enum class BatchedDisputeReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A batched dispute reservation. Its code is unique within a tenant. */
data class BatchedDisputeReservation(
    var code: String,
    var title: String,
    var limit: Int,
    var updatedAt: Int,
    var ownerId: String,
    var floorValue: Double,
    var discount: Double,
    var locale: String,
    var minimum: Double,
    var sequence: Int,
    var status: BatchedDisputeReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDisputeReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDisputeReservationStatus): Boolean {
        when (status) {
            BatchedDisputeReservationStatus.DRAFT -> return next == BatchedDisputeReservationStatus.PENDING
            BatchedDisputeReservationStatus.PENDING -> return next == BatchedDisputeReservationStatus.ACTIVE || next == BatchedDisputeReservationStatus.SUSPENDED
            BatchedDisputeReservationStatus.ACTIVE -> return next == BatchedDisputeReservationStatus.SUSPENDED
            BatchedDisputeReservationStatus.SUSPENDED -> {
                when (next) {
                    BatchedDisputeReservationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedDisputeReservationStatus) {
        if (!canMoveTo(next)) {
            error("batched dispute reservation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round(updatedAt.toDouble() * floorValue * 100) / 100

    fun snapshot(): BatchedDisputeReservation {
        return BatchedDisputeReservation(
            code = code,
            title = title,
            limit = limit,
            updatedAt = updatedAt,
            ownerId = ownerId,
            floorValue = floorValue,
            discount = discount,
            locale = locale,
            minimum = minimum,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a batched dispute reservation in status draft. */
fun newBatchedDisputeReservation(code: String, title: String): BatchedDisputeReservation {
    return BatchedDisputeReservation(
        code = code,
        title = title,
        limit = 0,
        updatedAt = 0,
        ownerId = "",
        floorValue = 0.0,
        discount = 0.0,
        locale = "",
        minimum = 0.0,
        sequence = 0,
        status = BatchedDisputeReservationStatus.DRAFT,
    )
}
