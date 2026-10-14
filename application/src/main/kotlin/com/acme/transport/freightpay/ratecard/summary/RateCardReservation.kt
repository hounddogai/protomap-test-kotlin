package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rate card reservation is in its life cycle. */
enum class RateCardReservationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rate card reservation as the rate card team keeps it. */
data class RateCardReservation(
    var code: String,
    var name: String,
    var ratio: Double,
    var expiresAt: Int,
    var routeCode: String,
    var retries: Int,
    var revision: Int,
    var status: RateCardReservationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardReservationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardReservationStatus): Boolean {
        when (status) {
            RateCardReservationStatus.NEW -> return next == RateCardReservationStatus.FAILED
            RateCardReservationStatus.QUEUED -> return next == RateCardReservationStatus.RUNNING
            RateCardReservationStatus.RUNNING -> return next == RateCardReservationStatus.DONE || next == RateCardReservationStatus.FAILED
            RateCardReservationStatus.DONE -> {
                when (next) {
                    RateCardReservationStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RateCardReservationStatus) {
        if (!canMoveTo(next)) {
            error("rate card reservation $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun adjustedTotal(): Double = round((ratio - retries.toDouble()) * 100) / 100

    fun snapshot(): RateCardReservation {
        return RateCardReservation(
            code = code,
            name = name,
            ratio = ratio,
            expiresAt = expiresAt,
            routeCode = routeCode,
            retries = retries,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rate card reservation in status new. */
fun newRateCardReservation(code: String, name: String): RateCardReservation {
    return RateCardReservation(
        code = code,
        name = name,
        ratio = 0.0,
        expiresAt = 0,
        routeCode = "",
        retries = 0,
        revision = 0,
        status = RateCardReservationStatus.NEW,
    )
}
