package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict capacity reservation is in its life cycle. */
enum class StrictCapacityReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A strict capacity reservation as the capacity team keeps it. */
data class StrictCapacityReservation(
    var code: String,
    var name: String,
    var stopCount: Int,
    var updatedAt: Int,
    var summary: String,
    var margin: Double,
    var amount: Double,
    var score: Double,
    var status: StrictCapacityReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCapacityReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCapacityReservationStatus): Boolean {
        when (status) {
            StrictCapacityReservationStatus.PLANNED -> {
                when (next) {
                    StrictCapacityReservationStatus.IN_PROGRESS, StrictCapacityReservationStatus.COMPLETED -> return true
                }
                return false
            }
            StrictCapacityReservationStatus.SCHEDULED -> {
                when (next) {
                    StrictCapacityReservationStatus.IN_PROGRESS, StrictCapacityReservationStatus.COMPLETED -> return true
                }
                return false
            }
            StrictCapacityReservationStatus.IN_PROGRESS -> return next == StrictCapacityReservationStatus.CANCELLED
            StrictCapacityReservationStatus.COMPLETED -> {
                when (next) {
                    StrictCapacityReservationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictCapacityReservationStatus) {
        if (!canMoveTo(next)) {
            error("strict capacity reservation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun effectiveValue(): Double = round(margin * amount * 100) / 100

    fun snapshot(): StrictCapacityReservation {
        return StrictCapacityReservation(
            code = code,
            name = name,
            stopCount = stopCount,
            updatedAt = updatedAt,
            summary = summary,
            margin = margin,
            amount = amount,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict capacity reservation in status planned. */
fun newStrictCapacityReservation(code: String, name: String): StrictCapacityReservation {
    return StrictCapacityReservation(
        code = code,
        name = name,
        stopCount = 0,
        updatedAt = 0,
        summary = "",
        margin = 0.0,
        amount = 0.0,
        score = 0.0,
        status = StrictCapacityReservationStatus.PLANNED,
    )
}
