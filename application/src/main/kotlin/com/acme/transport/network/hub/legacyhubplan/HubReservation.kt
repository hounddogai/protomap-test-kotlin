package com.acme.transport.network.hub.legacyhubplan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a hub reservation is in its life cycle. */
enum class HubReservationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A hub reservation as the hub team keeps it. */
data class HubReservation(
    var code: String,
    var name: String,
    var weight: Double,
    var depot: String,
    var trailerId: String,
    var floorValue: Double,
    var shiftCode: String,
    var endsAt: Int,
    var amount: Double,
    var minimum: Double,
    var status: HubReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubReservationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubReservationStatus): Boolean {
        when (status) {
            HubReservationStatus.NEW -> return next == HubReservationStatus.RUNNING
            HubReservationStatus.QUEUED -> {
                when (next) {
                    HubReservationStatus.FAILED -> return true
                }
                return false
            }
            HubReservationStatus.RUNNING -> {
                when (next) {
                    HubReservationStatus.DONE -> return true
                }
                return false
            }
            HubReservationStatus.DONE -> return next == HubReservationStatus.FAILED
        }
        return false
    }

    fun moveTo(next: HubReservationStatus) {
        if (!canMoveTo(next)) {
            error("hub reservation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"hub reservation"} $code"

    fun effectiveValue(): Double = round((minimum + endsAt.toDouble()) * 100) / 100

    fun snapshot(): HubReservation {
        return HubReservation(
            code = code,
            name = name,
            weight = weight,
            depot = depot,
            trailerId = trailerId,
            floorValue = floorValue,
            shiftCode = shiftCode,
            endsAt = endsAt,
            amount = amount,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a hub reservation in status new. */
fun newHubReservation(code: String, name: String): HubReservation {
    return HubReservation(
        code = code,
        name = name,
        weight = 0.0,
        depot = "",
        trailerId = "",
        floorValue = 0.0,
        shiftCode = "",
        endsAt = 0,
        amount = 0.0,
        minimum = 0.0,
        status = HubReservationStatus.NEW,
    )
}
