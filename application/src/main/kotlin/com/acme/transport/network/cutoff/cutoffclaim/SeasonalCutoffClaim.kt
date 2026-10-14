package com.acme.transport.network.cutoff.cutoffclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal cutoff claim records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a seasonal cutoff claim is in its life cycle. */
enum class SeasonalCutoffClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a seasonal cutoff claim and its current status. */
data class SeasonalCutoffClaim(
    var code: String,
    var name: String,
    var category: String,
    var minimum: Double,
    var rate: Double,
    var expiresAt: Int,
    var checksum: String,
    var budget: Double,
    var etaMinutes: Int,
    var status: SeasonalCutoffClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCutoffClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCutoffClaimStatus): Boolean {
        when (status) {
            SeasonalCutoffClaimStatus.DRAFT -> {
                when (next) {
                    SeasonalCutoffClaimStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalCutoffClaimStatus.PENDING -> return next == SeasonalCutoffClaimStatus.ACTIVE || next == SeasonalCutoffClaimStatus.CLOSED
            SeasonalCutoffClaimStatus.ACTIVE -> {
                when (next) {
                    SeasonalCutoffClaimStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalCutoffClaimStatus.SUSPENDED -> return next == SeasonalCutoffClaimStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SeasonalCutoffClaimStatus) {
        if (!canMoveTo(next)) {
            error("seasonal cutoff claim $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netValue(): Double = round((expiresAt.toDouble() + rate) * 100) / 100

    fun snapshot(): SeasonalCutoffClaim {
        return SeasonalCutoffClaim(
            code = code,
            name = name,
            category = category,
            minimum = minimum,
            rate = rate,
            expiresAt = expiresAt,
            checksum = checksum,
            budget = budget,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a seasonal cutoff claim in status draft. */
fun newSeasonalCutoffClaim(code: String, name: String): SeasonalCutoffClaim {
    return SeasonalCutoffClaim(
        code = code,
        name = name,
        category = "",
        minimum = 0.0,
        rate = 0.0,
        expiresAt = 0,
        checksum = "",
        budget = 0.0,
        etaMinutes = 0,
        status = SeasonalCutoffClaimStatus.DRAFT,
    )
}
