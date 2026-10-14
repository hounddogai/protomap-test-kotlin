package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient proof of delivery reservation is in its life cycle. */
enum class LenientProofOfDeliveryReservationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a lenient proof of delivery reservation and its current status. */
data class LenientProofOfDeliveryReservation(
    var code: String,
    var title: String,
    var startsAt: Int,
    var region: String,
    var vehicleId: String,
    var approved: Boolean,
    var label: String,
    var country: String,
    var odometer: Int,
    var cost: Double,
    var status: LenientProofOfDeliveryReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientProofOfDeliveryReservationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientProofOfDeliveryReservationStatus): Boolean {
        when (status) {
            LenientProofOfDeliveryReservationStatus.DRAFT -> return next == LenientProofOfDeliveryReservationStatus.ACTIVE
            LenientProofOfDeliveryReservationStatus.PENDING -> {
                when (next) {
                    LenientProofOfDeliveryReservationStatus.ACTIVE -> return true
                }
                return false
            }
            LenientProofOfDeliveryReservationStatus.ACTIVE -> {
                when (next) {
                    LenientProofOfDeliveryReservationStatus.CLOSED -> return true
                }
                return false
            }
            LenientProofOfDeliveryReservationStatus.SUSPENDED -> {
                when (next) {
                    LenientProofOfDeliveryReservationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientProofOfDeliveryReservationStatus) {
        if (!canMoveTo(next)) {
            error("lenient proof of delivery reservation $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"lenient proof of delivery reservation"} $code"

    fun weightedTotal(): Double = round(odometer.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): LenientProofOfDeliveryReservation {
        return LenientProofOfDeliveryReservation(
            code = code,
            title = title,
            startsAt = startsAt,
            region = region,
            vehicleId = vehicleId,
            approved = approved,
            label = label,
            country = country,
            odometer = odometer,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a lenient proof of delivery reservation in status draft. */
fun newLenientProofOfDeliveryReservation(code: String, title: String): LenientProofOfDeliveryReservation {
    return LenientProofOfDeliveryReservation(
        code = code,
        title = title,
        startsAt = 0,
        region = "",
        vehicleId = "",
        approved = false,
        label = "",
        country = "",
        odometer = 0,
        cost = 0.0,
        status = LenientProofOfDeliveryReservationStatus.DRAFT,
    )
}
