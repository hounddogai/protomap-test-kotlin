package com.acme.transport.linehaul.trailer.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The default trailer claim records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a default trailer claim is in its life cycle. */
enum class DefaultTrailerClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A default trailer claim as the trailer team keeps it. */
data class DefaultTrailerClaim(
    var code: String,
    var label: String,
    var floorValue: Double,
    var title: String,
    var fuelLiters: Double,
    var depot: String,
    var revision: Int,
    var status: DefaultTrailerClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultTrailerClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultTrailerClaimStatus): Boolean {
        when (status) {
            DefaultTrailerClaimStatus.OPEN -> {
                when (next) {
                    DefaultTrailerClaimStatus.REOPENED -> return true
                }
                return false
            }
            DefaultTrailerClaimStatus.IN_REVIEW -> return next == DefaultTrailerClaimStatus.RESOLVED || next == DefaultTrailerClaimStatus.ARCHIVED
            DefaultTrailerClaimStatus.RESOLVED -> {
                when (next) {
                    DefaultTrailerClaimStatus.REOPENED -> return true
                }
                return false
            }
            DefaultTrailerClaimStatus.REOPENED -> {
                when (next) {
                    DefaultTrailerClaimStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultTrailerClaimStatus) {
        if (!canMoveTo(next)) {
            error("default trailer claim $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedExposure(): Double = round(floorValue * fuelLiters * 100) / 100

    fun snapshot(): DefaultTrailerClaim {
        return DefaultTrailerClaim(
            code = code,
            label = label,
            floorValue = floorValue,
            title = title,
            fuelLiters = fuelLiters,
            depot = depot,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a default trailer claim in status open. */
fun newDefaultTrailerClaim(code: String, label: String): DefaultTrailerClaim {
    return DefaultTrailerClaim(
        code = code,
        label = label,
        floorValue = 0.0,
        title = "",
        fuelLiters = 0.0,
        depot = "",
        revision = 0,
        status = DefaultTrailerClaimStatus.OPEN,
    )
}
