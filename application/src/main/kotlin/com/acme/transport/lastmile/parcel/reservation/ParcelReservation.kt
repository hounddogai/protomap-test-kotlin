package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The parcel reservation records of lastmile's parcel work, and the rules, stores, and calculations around
// them.

/** Where a parcel reservation is in its life cycle. */
enum class ParcelReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a parcel reservation and its current status. */
data class ParcelReservation(
    var code: String,
    var title: String,
    var segment: String,
    var checksum: String,
    var capacity: Int,
    var unit: String,
    var attempts: Int,
    var archived: Boolean,
    var driverId: String,
    var score: Double,
    var status: ParcelReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ParcelReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ParcelReservationStatus): Boolean {
        when (status) {
            ParcelReservationStatus.OPEN -> return next == ParcelReservationStatus.IN_REVIEW
            ParcelReservationStatus.IN_REVIEW -> {
                when (next) {
                    ParcelReservationStatus.REOPENED, ParcelReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            ParcelReservationStatus.RESOLVED -> return next == ParcelReservationStatus.ARCHIVED
            ParcelReservationStatus.REOPENED -> {
                when (next) {
                    ParcelReservationStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ParcelReservationStatus) {
        if (!canMoveTo(next)) {
            error("parcel reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"parcel reservation"} $code"

    fun weightedExposure(): Double = round((score + capacity.toDouble()) * 100) / 100

    fun snapshot(): ParcelReservation {
        return ParcelReservation(
            code = code,
            title = title,
            segment = segment,
            checksum = checksum,
            capacity = capacity,
            unit = unit,
            attempts = attempts,
            archived = archived,
            driverId = driverId,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a parcel reservation in status open. */
fun newParcelReservation(code: String, title: String): ParcelReservation {
    return ParcelReservation(
        code = code,
        title = title,
        segment = "",
        checksum = "",
        capacity = 0,
        unit = "",
        attempts = 0,
        archived = false,
        driverId = "",
        score = 0.0,
        status = ParcelReservationStatus.OPEN,
    )
}
