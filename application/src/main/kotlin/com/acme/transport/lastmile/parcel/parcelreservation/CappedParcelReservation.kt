package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped parcel reservation records of lastmile's parcel work, and the rules, stores, and calculations
// around them.

/** Where a capped parcel reservation is in its life cycle. */
enum class CappedParcelReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a capped parcel reservation and its current status. */
data class CappedParcelReservation(
    var code: String,
    var title: String,
    var retries: Int,
    var windowDays: Int,
    var region: String,
    var segment: String,
    var discount: Double,
    var status: CappedParcelReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedParcelReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedParcelReservationStatus): Boolean {
        when (status) {
            CappedParcelReservationStatus.IDLE -> return next == CappedParcelReservationStatus.READY
            CappedParcelReservationStatus.WARMING -> {
                when (next) {
                    CappedParcelReservationStatus.DRAINING -> return true
                }
                return false
            }
            CappedParcelReservationStatus.READY -> {
                when (next) {
                    CappedParcelReservationStatus.DRAINING, CappedParcelReservationStatus.STOPPED -> return true
                }
                return false
            }
            CappedParcelReservationStatus.DRAINING -> {
                when (next) {
                    CappedParcelReservationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedParcelReservationStatus) {
        if (!canMoveTo(next)) {
            error("capped parcel reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedVolume(): Double = round((windowDays.toDouble() + discount) * 100) / 100

    fun snapshot(): CappedParcelReservation {
        return CappedParcelReservation(
            code = code,
            title = title,
            retries = retries,
            windowDays = windowDays,
            region = region,
            segment = segment,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a capped parcel reservation in status idle. */
fun newCappedParcelReservation(code: String, title: String): CappedParcelReservation {
    return CappedParcelReservation(
        code = code,
        title = title,
        retries = 0,
        windowDays = 0,
        region = "",
        segment = "",
        discount = 0.0,
        status = CappedParcelReservationStatus.IDLE,
    )
}
