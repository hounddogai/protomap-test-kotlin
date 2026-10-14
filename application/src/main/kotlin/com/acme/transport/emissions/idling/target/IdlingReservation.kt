package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a idling reservation is in its life cycle. */
enum class IdlingReservationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a idling reservation and its current status. */
data class IdlingReservation(
    var code: String,
    var title: String,
    var odometer: Int,
    var notes: String,
    var createdAt: Int,
    var status: IdlingReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IdlingReservationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: IdlingReservationStatus): Boolean {
        when (status) {
            IdlingReservationStatus.PLANNED -> return next == IdlingReservationStatus.CANCELLED
            IdlingReservationStatus.SCHEDULED -> {
                when (next) {
                    IdlingReservationStatus.IN_PROGRESS, IdlingReservationStatus.CANCELLED -> return true
                }
                return false
            }
            IdlingReservationStatus.IN_PROGRESS -> {
                when (next) {
                    IdlingReservationStatus.CANCELLED -> return true
                }
                return false
            }
            IdlingReservationStatus.COMPLETED -> return next == IdlingReservationStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: IdlingReservationStatus) {
        if (!canMoveTo(next)) {
            error("idling reservation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun weightedLoad(): Double = round((createdAt.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): IdlingReservation {
        return IdlingReservation(
            code = code,
            title = title,
            odometer = odometer,
            notes = notes,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a idling reservation in status planned. */
fun newIdlingReservation(code: String, title: String): IdlingReservation {
    return IdlingReservation(
        code = code,
        title = title,
        odometer = 0,
        notes = "",
        createdAt = 0,
        status = IdlingReservationStatus.PLANNED,
    )
}
