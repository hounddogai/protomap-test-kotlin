package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal parcel reservation records of lastmile's parcel work, and the rules, stores, and calculations
// around them.

/** Where a seasonal parcel reservation is in its life cycle. */
enum class SeasonalParcelReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One seasonal parcel reservation, with what the planners need to act on it. */
data class SeasonalParcelReservation(
    var code: String,
    var label: String,
    var notes: String,
    var name: String,
    var cost: Double,
    var country: String,
    var ceiling: Double,
    var segment: String,
    var status: SeasonalParcelReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalParcelReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalParcelReservationStatus): Boolean {
        when (status) {
            SeasonalParcelReservationStatus.PLANNED -> return next == SeasonalParcelReservationStatus.IN_PROGRESS || next == SeasonalParcelReservationStatus.CANCELLED
            SeasonalParcelReservationStatus.SCHEDULED -> {
                when (next) {
                    SeasonalParcelReservationStatus.COMPLETED, SeasonalParcelReservationStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalParcelReservationStatus.IN_PROGRESS -> return next == SeasonalParcelReservationStatus.COMPLETED
            SeasonalParcelReservationStatus.COMPLETED -> return next == SeasonalParcelReservationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalParcelReservationStatus) {
        if (!canMoveTo(next)) {
            error("seasonal parcel reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal parcel reservation"} $code"

    fun adjustedExposure(): Double = round((ceiling - cost) * 100) / 100

    fun snapshot(): SeasonalParcelReservation {
        return SeasonalParcelReservation(
            code = code,
            label = label,
            notes = notes,
            name = name,
            cost = cost,
            country = country,
            ceiling = ceiling,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a seasonal parcel reservation in status planned. */
fun newSeasonalParcelReservation(code: String, label: String): SeasonalParcelReservation {
    return SeasonalParcelReservation(
        code = code,
        label = label,
        notes = "",
        name = "",
        cost = 0.0,
        country = "",
        ceiling = 0.0,
        segment = "",
        status = SeasonalParcelReservationStatus.PLANNED,
    )
}
