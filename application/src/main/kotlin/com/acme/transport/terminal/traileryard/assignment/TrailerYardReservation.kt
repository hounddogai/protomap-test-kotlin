package com.acme.transport.terminal.traileryard.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard reservation is in its life cycle. */
enum class TrailerYardReservationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A trailer yard reservation as the trailer yard team keeps it. */
data class TrailerYardReservation(
    var code: String,
    var title: String,
    var odometer: Int,
    var stopCount: Int,
    var batchSize: Int,
    var windowDays: Int,
    var region: String,
    var currency: String,
    var status: TrailerYardReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardReservationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardReservationStatus): Boolean {
        when (status) {
            TrailerYardReservationStatus.REQUESTED -> return next == TrailerYardReservationStatus.GRANTED || next == TrailerYardReservationStatus.EXPIRED
            TrailerYardReservationStatus.GRANTED -> {
                when (next) {
                    TrailerYardReservationStatus.REVOKED -> return true
                }
                return false
            }
            TrailerYardReservationStatus.REVOKED -> {
                when (next) {
                    TrailerYardReservationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerYardReservationStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard reservation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"trailer yard reservation"} $code"

    fun weightedValue(): Double = round((stopCount.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): TrailerYardReservation {
        return TrailerYardReservation(
            code = code,
            title = title,
            odometer = odometer,
            stopCount = stopCount,
            batchSize = batchSize,
            windowDays = windowDays,
            region = region,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a trailer yard reservation in status requested. */
fun newTrailerYardReservation(code: String, title: String): TrailerYardReservation {
    return TrailerYardReservation(
        code = code,
        title = title,
        odometer = 0,
        stopCount = 0,
        batchSize = 0,
        windowDays = 0,
        region = "",
        currency = "",
        status = TrailerYardReservationStatus.REQUESTED,
    )
}
