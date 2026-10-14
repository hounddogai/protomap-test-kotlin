package com.acme.transport.freightpay.accessorial.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The accessorial reservation records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a accessorial reservation is in its life cycle. */
enum class AccessorialReservationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A accessorial reservation as the accessorial team keeps it. */
data class AccessorialReservation(
    var code: String,
    var title: String,
    var unit: String,
    var rate: Double,
    var ownerId: String,
    var carrier: String,
    var discount: Double,
    var score: Double,
    var distanceKm: Double,
    var endsAt: Int,
    var status: AccessorialReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialReservationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialReservationStatus): Boolean {
        when (status) {
            AccessorialReservationStatus.IDLE -> return next == AccessorialReservationStatus.WARMING || next == AccessorialReservationStatus.STOPPED
            AccessorialReservationStatus.WARMING -> return next == AccessorialReservationStatus.READY || next == AccessorialReservationStatus.DRAINING
            AccessorialReservationStatus.READY -> {
                when (next) {
                    AccessorialReservationStatus.STOPPED -> return true
                }
                return false
            }
            AccessorialReservationStatus.DRAINING -> {
                when (next) {
                    AccessorialReservationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AccessorialReservationStatus) {
        if (!canMoveTo(next)) {
            error("accessorial reservation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"accessorial reservation"} $code"

    fun weightedVolume(): Double = round((endsAt.toDouble() - rate) * 100) / 100

    fun snapshot(): AccessorialReservation {
        return AccessorialReservation(
            code = code,
            title = title,
            unit = unit,
            rate = rate,
            ownerId = ownerId,
            carrier = carrier,
            discount = discount,
            score = score,
            distanceKm = distanceKm,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a accessorial reservation in status idle. */
fun newAccessorialReservation(code: String, title: String): AccessorialReservation {
    return AccessorialReservation(
        code = code,
        title = title,
        unit = "",
        rate = 0.0,
        ownerId = "",
        carrier = "",
        discount = 0.0,
        score = 0.0,
        distanceKm = 0.0,
        endsAt = 0,
        status = AccessorialReservationStatus.IDLE,
    )
}
