package com.acme.transport.crewing.roster.rosterforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict roster reservation is in its life cycle. */
enum class StrictRosterReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a strict roster reservation and its current status. */
data class StrictRosterReservation(
    var code: String,
    var name: String,
    var stopCount: Int,
    var notes: String,
    var expiresAt: Int,
    var startsAt: Int,
    var ratio: Double,
    var status: StrictRosterReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRosterReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRosterReservationStatus): Boolean {
        when (status) {
            StrictRosterReservationStatus.DRAFT -> return next == StrictRosterReservationStatus.PENDING
            StrictRosterReservationStatus.PENDING -> return next == StrictRosterReservationStatus.SUSPENDED
            StrictRosterReservationStatus.ACTIVE -> {
                when (next) {
                    StrictRosterReservationStatus.SUSPENDED, StrictRosterReservationStatus.CLOSED -> return true
                }
                return false
            }
            StrictRosterReservationStatus.SUSPENDED -> {
                when (next) {
                    StrictRosterReservationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictRosterReservationStatus) {
        if (!canMoveTo(next)) {
            error("strict roster reservation $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"strict roster reservation"} $code"

    fun netVolume(): Double = round((expiresAt.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): StrictRosterReservation {
        return StrictRosterReservation(
            code = code,
            name = name,
            stopCount = stopCount,
            notes = notes,
            expiresAt = expiresAt,
            startsAt = startsAt,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict roster reservation in status draft. */
fun newStrictRosterReservation(code: String, name: String): StrictRosterReservation {
    return StrictRosterReservation(
        code = code,
        name = name,
        stopCount = 0,
        notes = "",
        expiresAt = 0,
        startsAt = 0,
        ratio = 0.0,
        status = StrictRosterReservationStatus.DRAFT,
    )
}
