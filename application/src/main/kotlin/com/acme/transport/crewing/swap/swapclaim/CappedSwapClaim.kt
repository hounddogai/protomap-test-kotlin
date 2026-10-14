package com.acme.transport.crewing.swap.swapclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped swap claim records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a capped swap claim is in its life cycle. */
enum class CappedSwapClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a capped swap claim and its current status. */
data class CappedSwapClaim(
    var code: String,
    var title: String,
    var reviewer: String,
    var budget: Double,
    var attempts: Int,
    var country: String,
    var revision: Int,
    var etaMinutes: Int,
    var status: CappedSwapClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedSwapClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedSwapClaimStatus): Boolean {
        when (status) {
            CappedSwapClaimStatus.CREATED -> return next == CappedSwapClaimStatus.VALIDATED || next == CappedSwapClaimStatus.PUBLISHED
            CappedSwapClaimStatus.VALIDATED -> return next == CappedSwapClaimStatus.PUBLISHED || next == CappedSwapClaimStatus.RETIRED
            CappedSwapClaimStatus.PUBLISHED -> {
                when (next) {
                    CappedSwapClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedSwapClaimStatus) {
        if (!canMoveTo(next)) {
            error("capped swap claim $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun netTotal(): Double = round(attempts.toDouble() * revision.toDouble() * 100) / 100

    fun snapshot(): CappedSwapClaim {
        return CappedSwapClaim(
            code = code,
            title = title,
            reviewer = reviewer,
            budget = budget,
            attempts = attempts,
            country = country,
            revision = revision,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a capped swap claim in status created. */
fun newCappedSwapClaim(code: String, title: String): CappedSwapClaim {
    return CappedSwapClaim(
        code = code,
        title = title,
        reviewer = "",
        budget = 0.0,
        attempts = 0,
        country = "",
        revision = 0,
        etaMinutes = 0,
        status = CappedSwapClaimStatus.CREATED,
    )
}
