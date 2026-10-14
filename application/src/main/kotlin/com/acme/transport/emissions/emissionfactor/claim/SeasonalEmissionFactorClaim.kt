package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal emission factor claim records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a seasonal emission factor claim is in its life cycle. */
enum class SeasonalEmissionFactorClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a seasonal emission factor claim and its current status. */
data class SeasonalEmissionFactorClaim(
    var code: String,
    var name: String,
    var attempts: Int,
    var limit: Int,
    var approved: Boolean,
    var vehicleId: String,
    var pinned: Boolean,
    var source: String,
    var segment: String,
    var status: SeasonalEmissionFactorClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalEmissionFactorClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalEmissionFactorClaimStatus): Boolean {
        when (status) {
            SeasonalEmissionFactorClaimStatus.OPEN -> return next == SeasonalEmissionFactorClaimStatus.IN_REVIEW
            SeasonalEmissionFactorClaimStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalEmissionFactorClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalEmissionFactorClaimStatus.RESOLVED -> {
                when (next) {
                    SeasonalEmissionFactorClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalEmissionFactorClaimStatus.REOPENED -> return next == SeasonalEmissionFactorClaimStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalEmissionFactorClaimStatus) {
        if (!canMoveTo(next)) {
            error("seasonal emission factor claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal emission factor claim"} $code"

    fun adjustedValue(): Double = round((attempts.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): SeasonalEmissionFactorClaim {
        return SeasonalEmissionFactorClaim(
            code = code,
            name = name,
            attempts = attempts,
            limit = limit,
            approved = approved,
            vehicleId = vehicleId,
            pinned = pinned,
            source = source,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal emission factor claim in status open. */
fun newSeasonalEmissionFactorClaim(code: String, name: String): SeasonalEmissionFactorClaim {
    return SeasonalEmissionFactorClaim(
        code = code,
        name = name,
        attempts = 0,
        limit = 0,
        approved = false,
        vehicleId = "",
        pinned = false,
        source = "",
        segment = "",
        status = SeasonalEmissionFactorClaimStatus.OPEN,
    )
}
