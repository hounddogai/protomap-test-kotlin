package com.acme.transport.freightpay.ratecard.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive rate card reservation is in its life cycle. */
enum class AdaptiveRateCardReservationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a adaptive rate card reservation and its current status. */
data class AdaptiveRateCardReservation(
    var code: String,
    var name: String,
    var revision: Int,
    var lane: String,
    var driverId: String,
    var country: String,
    var vehicleId: String,
    var loadKg: Double,
    var summary: String,
    var status: AdaptiveRateCardReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRateCardReservationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRateCardReservationStatus): Boolean {
        when (status) {
            AdaptiveRateCardReservationStatus.REQUESTED -> return next == AdaptiveRateCardReservationStatus.REVOKED
            AdaptiveRateCardReservationStatus.GRANTED -> {
                when (next) {
                    AdaptiveRateCardReservationStatus.REVOKED -> return true
                }
                return false
            }
            AdaptiveRateCardReservationStatus.REVOKED -> {
                when (next) {
                    AdaptiveRateCardReservationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveRateCardReservationStatus) {
        if (!canMoveTo(next)) {
            error("adaptive rate card reservation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"adaptive rate card reservation"} $code"

    fun effectiveTotal(): Double = round((loadKg - revision.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveRateCardReservation {
        return AdaptiveRateCardReservation(
            code = code,
            name = name,
            revision = revision,
            lane = lane,
            driverId = driverId,
            country = country,
            vehicleId = vehicleId,
            loadKg = loadKg,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a adaptive rate card reservation in status requested. */
fun newAdaptiveRateCardReservation(code: String, name: String): AdaptiveRateCardReservation {
    return AdaptiveRateCardReservation(
        code = code,
        name = name,
        revision = 0,
        lane = "",
        driverId = "",
        country = "",
        vehicleId = "",
        loadKg = 0.0,
        summary = "",
        status = AdaptiveRateCardReservationStatus.REQUESTED,
    )
}
