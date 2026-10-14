package com.acme.transport.network.cutoff.ledger

import com.acme.transport.kit.text.normalize

/** Where a batched cutoff reservation is in its life cycle. */
enum class BatchedCutoffReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A batched cutoff reservation. Its code is unique within a tenant. */
data class BatchedCutoffReservation(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var externalRef: String,
    var driverId: String,
    var channel: String,
    var status: BatchedCutoffReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCutoffReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCutoffReservationStatus): Boolean {
        when (status) {
            BatchedCutoffReservationStatus.DRAFT -> {
                when (next) {
                    BatchedCutoffReservationStatus.ACTIVE -> return true
                }
                return false
            }
            BatchedCutoffReservationStatus.PENDING -> return next == BatchedCutoffReservationStatus.CLOSED
            BatchedCutoffReservationStatus.ACTIVE -> return next == BatchedCutoffReservationStatus.SUSPENDED || next == BatchedCutoffReservationStatus.CLOSED
            BatchedCutoffReservationStatus.SUSPENDED -> {
                when (next) {
                    BatchedCutoffReservationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedCutoffReservationStatus) {
        if (!canMoveTo(next)) {
            error("batched cutoff reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): BatchedCutoffReservation {
        return BatchedCutoffReservation(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            externalRef = externalRef,
            driverId = driverId,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a batched cutoff reservation in status draft. */
fun newBatchedCutoffReservation(code: String, name: String): BatchedCutoffReservation {
    return BatchedCutoffReservation(
        code = code,
        name = name,
        fuelLiters = 0.0,
        externalRef = "",
        driverId = "",
        channel = "",
        status = BatchedCutoffReservationStatus.DRAFT,
    )
}
