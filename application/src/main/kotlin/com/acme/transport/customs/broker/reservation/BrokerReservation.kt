package com.acme.transport.customs.broker.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The broker reservation records of customs's broker work, and the rules, stores, and calculations around
// them.

/** Where a broker reservation is in its life cycle. */
enum class BrokerReservationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One broker reservation, with what the planners need to act on it. */
data class BrokerReservation(
    var code: String,
    var title: String,
    var windowDays: Int,
    var ownerId: String,
    var checksum: String,
    var contactEmail: String,
    var amount: Double,
    var routeCode: String,
    var status: BrokerReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BrokerReservationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BrokerReservationStatus): Boolean {
        when (status) {
            BrokerReservationStatus.REQUESTED -> return next == BrokerReservationStatus.GRANTED
            BrokerReservationStatus.GRANTED -> return next == BrokerReservationStatus.REVOKED
            BrokerReservationStatus.REVOKED -> {
                when (next) {
                    BrokerReservationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BrokerReservationStatus) {
        if (!canMoveTo(next)) {
            error("broker reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun adjustedExposure(): Double = round(windowDays.toDouble() * amount * 100) / 100

    fun snapshot(): BrokerReservation {
        return BrokerReservation(
            code = code,
            title = title,
            windowDays = windowDays,
            ownerId = ownerId,
            checksum = checksum,
            contactEmail = contactEmail,
            amount = amount,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a broker reservation in status requested. */
fun newBrokerReservation(code: String, title: String): BrokerReservation {
    return BrokerReservation(
        code = code,
        title = title,
        windowDays = 0,
        ownerId = "",
        checksum = "",
        contactEmail = "",
        amount = 0.0,
        routeCode = "",
        status = BrokerReservationStatus.REQUESTED,
    )
}
