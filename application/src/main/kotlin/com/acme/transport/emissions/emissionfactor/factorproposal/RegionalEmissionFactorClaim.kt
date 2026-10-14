package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional emission factor claim is in its life cycle. */
enum class RegionalEmissionFactorClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional emission factor claim as the emission factor team keeps it. */
data class RegionalEmissionFactorClaim(
    var code: String,
    var label: String,
    var locale: String,
    var archived: Boolean,
    var quantity: Int,
    var trailerId: String,
    var retries: Int,
    var status: RegionalEmissionFactorClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalEmissionFactorClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalEmissionFactorClaimStatus): Boolean {
        when (status) {
            RegionalEmissionFactorClaimStatus.DRAFT -> return next == RegionalEmissionFactorClaimStatus.SUSPENDED || next == RegionalEmissionFactorClaimStatus.CLOSED
            RegionalEmissionFactorClaimStatus.PENDING -> {
                when (next) {
                    RegionalEmissionFactorClaimStatus.ACTIVE, RegionalEmissionFactorClaimStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalEmissionFactorClaimStatus.ACTIVE -> return next == RegionalEmissionFactorClaimStatus.SUSPENDED || next == RegionalEmissionFactorClaimStatus.CLOSED
            RegionalEmissionFactorClaimStatus.SUSPENDED -> {
                when (next) {
                    RegionalEmissionFactorClaimStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalEmissionFactorClaimStatus) {
        if (!canMoveTo(next)) {
            error("regional emission factor claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedExposure(): Double = round((retries.toDouble() - quantity.toDouble()) * 100) / 100

    fun snapshot(): RegionalEmissionFactorClaim {
        return RegionalEmissionFactorClaim(
            code = code,
            label = label,
            locale = locale,
            archived = archived,
            quantity = quantity,
            trailerId = trailerId,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a regional emission factor claim in status draft. */
fun newRegionalEmissionFactorClaim(code: String, label: String): RegionalEmissionFactorClaim {
    return RegionalEmissionFactorClaim(
        code = code,
        label = label,
        locale = "",
        archived = false,
        quantity = 0,
        trailerId = "",
        retries = 0,
        status = RegionalEmissionFactorClaimStatus.DRAFT,
    )
}
