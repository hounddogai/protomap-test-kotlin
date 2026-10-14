package com.acme.transport.crewing.restbreak.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict rest break claim records of crewing's rest break work, and the rules, stores, and calculations
// around them.

/** Where a strict rest break claim is in its life cycle. */
enum class StrictRestBreakClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One strict rest break claim, with what the planners need to act on it. */
data class StrictRestBreakClaim(
    var code: String,
    var title: String,
    var trailerId: String,
    var maximum: Double,
    var expiresAt: Int,
    var discount: Double,
    var driverId: String,
    var depot: String,
    var unit: String,
    var distanceKm: Double,
    var status: StrictRestBreakClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictRestBreakClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictRestBreakClaimStatus): Boolean {
        when (status) {
            StrictRestBreakClaimStatus.IDLE -> return next == StrictRestBreakClaimStatus.DRAINING || next == StrictRestBreakClaimStatus.STOPPED
            StrictRestBreakClaimStatus.WARMING -> {
                when (next) {
                    StrictRestBreakClaimStatus.READY -> return true
                }
                return false
            }
            StrictRestBreakClaimStatus.READY -> {
                when (next) {
                    StrictRestBreakClaimStatus.DRAINING, StrictRestBreakClaimStatus.STOPPED -> return true
                }
                return false
            }
            StrictRestBreakClaimStatus.DRAINING -> {
                when (next) {
                    StrictRestBreakClaimStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictRestBreakClaimStatus) {
        if (!canMoveTo(next)) {
            error("strict rest break claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun adjustedValue(): Double = round((discount - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): StrictRestBreakClaim {
        return StrictRestBreakClaim(
            code = code,
            title = title,
            trailerId = trailerId,
            maximum = maximum,
            expiresAt = expiresAt,
            discount = discount,
            driverId = driverId,
            depot = depot,
            unit = unit,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict rest break claim in status idle. */
fun newStrictRestBreakClaim(code: String, title: String): StrictRestBreakClaim {
    return StrictRestBreakClaim(
        code = code,
        title = title,
        trailerId = "",
        maximum = 0.0,
        expiresAt = 0,
        discount = 0.0,
        driverId = "",
        depot = "",
        unit = "",
        distanceKm = 0.0,
        status = StrictRestBreakClaimStatus.IDLE,
    )
}
