package com.acme.transport.network.transittime.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The transit time claim records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time claim is in its life cycle. */
enum class TransitTimeClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a transit time claim and its current status. */
data class TransitTimeClaim(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var graceDays: Int,
    var pinned: Boolean,
    var status: TransitTimeClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeClaimStatus): Boolean {
        when (status) {
            TransitTimeClaimStatus.DRAFT -> {
                when (next) {
                    TransitTimeClaimStatus.CLOSED -> return true
                }
                return false
            }
            TransitTimeClaimStatus.PENDING -> {
                when (next) {
                    TransitTimeClaimStatus.SUSPENDED -> return true
                }
                return false
            }
            TransitTimeClaimStatus.ACTIVE -> return next == TransitTimeClaimStatus.CLOSED
            TransitTimeClaimStatus.SUSPENDED -> return next == TransitTimeClaimStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TransitTimeClaimStatus) {
        if (!canMoveTo(next)) {
            error("transit time claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round((distanceKm - graceDays.toDouble()) * 100) / 100

    fun snapshot(): TransitTimeClaim {
        return TransitTimeClaim(
            code = code,
            name = name,
            distanceKm = distanceKm,
            graceDays = graceDays,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a transit time claim in status draft. */
fun newTransitTimeClaim(code: String, name: String): TransitTimeClaim {
    return TransitTimeClaim(
        code = code,
        name = name,
        distanceKm = 0.0,
        graceDays = 0,
        pinned = false,
        status = TransitTimeClaimStatus.DRAFT,
    )
}
