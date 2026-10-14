package com.acme.transport.lastmile.redelivery.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery reservation is in its life cycle. */
enum class RedeliveryReservationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a redelivery reservation and its current status. */
data class RedeliveryReservation(
    var code: String,
    var name: String,
    var notes: String,
    var pinned: Boolean,
    var summary: String,
    var fuelLiters: Double,
    var depot: String,
    var windowDays: Int,
    var minimum: Double,
    var quantity: Int,
    var channel: String,
    var status: RedeliveryReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryReservationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryReservationStatus): Boolean {
        when (status) {
            RedeliveryReservationStatus.OPEN -> {
                when (next) {
                    RedeliveryReservationStatus.IN_REVIEW, RedeliveryReservationStatus.RESOLVED -> return true
                }
                return false
            }
            RedeliveryReservationStatus.IN_REVIEW -> {
                when (next) {
                    RedeliveryReservationStatus.REOPENED, RedeliveryReservationStatus.ARCHIVED -> return true
                }
                return false
            }
            RedeliveryReservationStatus.RESOLVED -> return next == RedeliveryReservationStatus.ARCHIVED
            RedeliveryReservationStatus.REOPENED -> return next == RedeliveryReservationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RedeliveryReservationStatus) {
        if (!canMoveTo(next)) {
            error("redelivery reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"redelivery reservation"} $code"

    fun weightedValue(): Double = round((minimum - fuelLiters) * 100) / 100

    fun snapshot(): RedeliveryReservation {
        return RedeliveryReservation(
            code = code,
            name = name,
            notes = notes,
            pinned = pinned,
            summary = summary,
            fuelLiters = fuelLiters,
            depot = depot,
            windowDays = windowDays,
            minimum = minimum,
            quantity = quantity,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a redelivery reservation in status open. */
fun newRedeliveryReservation(code: String, name: String): RedeliveryReservation {
    return RedeliveryReservation(
        code = code,
        name = name,
        notes = "",
        pinned = false,
        summary = "",
        fuelLiters = 0.0,
        depot = "",
        windowDays = 0,
        minimum = 0.0,
        quantity = 0,
        channel = "",
        status = RedeliveryReservationStatus.OPEN,
    )
}
