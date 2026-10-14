package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn reservation is in its life cycle. */
enum class FuelBurnReservationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One fuel burn reservation, with what the planners need to act on it. */
data class FuelBurnReservation(
    var code: String,
    var title: String,
    var budget: Double,
    var weight: Double,
    var notes: String,
    var priority: Int,
    var channel: String,
    var rate: Double,
    var status: FuelBurnReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnReservationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnReservationStatus): Boolean {
        when (status) {
            FuelBurnReservationStatus.REQUESTED -> return next == FuelBurnReservationStatus.REVOKED
            FuelBurnReservationStatus.GRANTED -> {
                when (next) {
                    FuelBurnReservationStatus.REVOKED, FuelBurnReservationStatus.EXPIRED -> return true
                }
                return false
            }
            FuelBurnReservationStatus.REVOKED -> return next == FuelBurnReservationStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FuelBurnReservationStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fuel burn reservation"} $code"

    fun projectedTotal(): Double = round((rate - priority.toDouble()) * 100) / 100

    fun snapshot(): FuelBurnReservation {
        return FuelBurnReservation(
            code = code,
            title = title,
            budget = budget,
            weight = weight,
            notes = notes,
            priority = priority,
            channel = channel,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fuel burn reservation in status requested. */
fun newFuelBurnReservation(code: String, title: String): FuelBurnReservation {
    return FuelBurnReservation(
        code = code,
        title = title,
        budget = 0.0,
        weight = 0.0,
        notes = "",
        priority = 0,
        channel = "",
        rate = 0.0,
        status = FuelBurnReservationStatus.REQUESTED,
    )
}
