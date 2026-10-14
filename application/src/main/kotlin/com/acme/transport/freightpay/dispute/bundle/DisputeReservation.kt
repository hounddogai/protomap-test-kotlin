package com.acme.transport.freightpay.dispute.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dispute reservation is in its life cycle. */
enum class DisputeReservationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A dispute reservation as the dispute team keeps it. */
data class DisputeReservation(
    var code: String,
    var title: String,
    var discount: Double,
    var routeCode: String,
    var currency: String,
    var threshold: Double,
    var notes: String,
    var endsAt: Int,
    var score: Double,
    var channel: String,
    var status: DisputeReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeReservationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeReservationStatus): Boolean {
        when (status) {
            DisputeReservationStatus.REQUESTED -> return next == DisputeReservationStatus.GRANTED || next == DisputeReservationStatus.EXPIRED
            DisputeReservationStatus.GRANTED -> return next == DisputeReservationStatus.REVOKED
            DisputeReservationStatus.REVOKED -> {
                when (next) {
                    DisputeReservationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeReservationStatus) {
        if (!canMoveTo(next)) {
            error("dispute reservation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"dispute reservation"} $code"

    fun effectiveVolume(): Double = round((score - endsAt.toDouble()) * 100) / 100

    fun snapshot(): DisputeReservation {
        return DisputeReservation(
            code = code,
            title = title,
            discount = discount,
            routeCode = routeCode,
            currency = currency,
            threshold = threshold,
            notes = notes,
            endsAt = endsAt,
            score = score,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a dispute reservation in status requested. */
fun newDisputeReservation(code: String, title: String): DisputeReservation {
    return DisputeReservation(
        code = code,
        title = title,
        discount = 0.0,
        routeCode = "",
        currency = "",
        threshold = 0.0,
        notes = "",
        endsAt = 0,
        score = 0.0,
        channel = "",
        status = DisputeReservationStatus.REQUESTED,
    )
}
