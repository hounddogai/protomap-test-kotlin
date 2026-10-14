package com.acme.transport.crewing.availability.availabilityreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped availability reservation records of crewing's availability work, and the rules, stores, and
// calculations around them.

/** Where a capped availability reservation is in its life cycle. */
enum class CappedAvailabilityReservationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One capped availability reservation, with what the planners need to act on it. */
data class CappedAvailabilityReservation(
    var code: String,
    var name: String,
    var shiftCode: String,
    var contactEmail: String,
    var locale: String,
    var batchSize: Int,
    var notes: String,
    var distanceKm: Double,
    var minimum: Double,
    var startsAt: Int,
    var createdAt: Int,
    var status: CappedAvailabilityReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAvailabilityReservationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAvailabilityReservationStatus): Boolean {
        when (status) {
            CappedAvailabilityReservationStatus.CREATED -> {
                when (next) {
                    CappedAvailabilityReservationStatus.VALIDATED, CappedAvailabilityReservationStatus.PUBLISHED -> return true
                }
                return false
            }
            CappedAvailabilityReservationStatus.VALIDATED -> return next == CappedAvailabilityReservationStatus.RETIRED
            CappedAvailabilityReservationStatus.PUBLISHED -> {
                when (next) {
                    CappedAvailabilityReservationStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedAvailabilityReservationStatus) {
        if (!canMoveTo(next)) {
            error("capped availability reservation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round((createdAt.toDouble() + startsAt.toDouble()) * 100) / 100

    fun snapshot(): CappedAvailabilityReservation {
        return CappedAvailabilityReservation(
            code = code,
            name = name,
            shiftCode = shiftCode,
            contactEmail = contactEmail,
            locale = locale,
            batchSize = batchSize,
            notes = notes,
            distanceKm = distanceKm,
            minimum = minimum,
            startsAt = startsAt,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a capped availability reservation in status created. */
fun newCappedAvailabilityReservation(code: String, name: String): CappedAvailabilityReservation {
    return CappedAvailabilityReservation(
        code = code,
        name = name,
        shiftCode = "",
        contactEmail = "",
        locale = "",
        batchSize = 0,
        notes = "",
        distanceKm = 0.0,
        minimum = 0.0,
        startsAt = 0,
        createdAt = 0,
        status = CappedAvailabilityReservationStatus.CREATED,
    )
}
