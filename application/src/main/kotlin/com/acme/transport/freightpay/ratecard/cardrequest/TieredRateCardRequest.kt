package com.acme.transport.freightpay.ratecard.cardrequest

import com.acme.transport.kit.text.normalize

// The tiered rate card request records of freightpay's rate card work, and the rules, stores, and
// calculations around them.

/** Where a tiered rate card request is in its life cycle. */
enum class TieredRateCardRequestStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a tiered rate card request and its current status. */
data class TieredRateCardRequest(
    var code: String,
    var name: String,
    var driverId: String,
    var source: String,
    var fuelLiters: Double,
    var status: TieredRateCardRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredRateCardRequestStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredRateCardRequestStatus): Boolean {
        when (status) {
            TieredRateCardRequestStatus.REQUESTED -> {
                when (next) {
                    TieredRateCardRequestStatus.REVOKED, TieredRateCardRequestStatus.EXPIRED -> return true
                }
                return false
            }
            TieredRateCardRequestStatus.GRANTED -> {
                when (next) {
                    TieredRateCardRequestStatus.REVOKED, TieredRateCardRequestStatus.EXPIRED -> return true
                }
                return false
            }
            TieredRateCardRequestStatus.REVOKED -> return next == TieredRateCardRequestStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TieredRateCardRequestStatus) {
        if (!canMoveTo(next)) {
            error("tiered rate card request $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered rate card request"} $code"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): TieredRateCardRequest {
        return TieredRateCardRequest(
            code = code,
            name = name,
            driverId = driverId,
            source = source,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tiered rate card request in status requested. */
fun newTieredRateCardRequest(code: String, name: String): TieredRateCardRequest {
    return TieredRateCardRequest(
        code = code,
        name = name,
        driverId = "",
        source = "",
        fuelLiters = 0.0,
        status = TieredRateCardRequestStatus.REQUESTED,
    )
}
