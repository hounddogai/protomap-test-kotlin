package com.acme.transport.crewing.availability.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability reservation records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a availability reservation is in its life cycle. */
enum class AvailabilityReservationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a availability reservation and its current status. */
data class AvailabilityReservation(
    var code: String,
    var label: String,
    var driverId: String,
    var threshold: Double,
    var ceiling: Double,
    var sequence: Int,
    var distanceKm: Double,
    var minimum: Double,
    var cost: Double,
    var status: AvailabilityReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityReservationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityReservationStatus): Boolean {
        when (status) {
            AvailabilityReservationStatus.PROPOSED -> return next == AvailabilityReservationStatus.REJECTED
            AvailabilityReservationStatus.APPROVED -> return next == AvailabilityReservationStatus.REJECTED
            AvailabilityReservationStatus.REJECTED -> return next == AvailabilityReservationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AvailabilityReservationStatus) {
        if (!canMoveTo(next)) {
            error("availability reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveValue(): Double = round((cost + distanceKm) * 100) / 100

    fun snapshot(): AvailabilityReservation {
        return AvailabilityReservation(
            code = code,
            label = label,
            driverId = driverId,
            threshold = threshold,
            ceiling = ceiling,
            sequence = sequence,
            distanceKm = distanceKm,
            minimum = minimum,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a availability reservation in status proposed. */
fun newAvailabilityReservation(code: String, label: String): AvailabilityReservation {
    return AvailabilityReservation(
        code = code,
        label = label,
        driverId = "",
        threshold = 0.0,
        ceiling = 0.0,
        sequence = 0,
        distanceKm = 0.0,
        minimum = 0.0,
        cost = 0.0,
        status = AvailabilityReservationStatus.PROPOSED,
    )
}
