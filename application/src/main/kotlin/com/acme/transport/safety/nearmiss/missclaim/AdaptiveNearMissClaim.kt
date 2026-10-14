package com.acme.transport.safety.nearmiss.missclaim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive near miss claim records of safety's near miss work, and the rules, stores, and calculations
// around them.

/** Where a adaptive near miss claim is in its life cycle. */
enum class AdaptiveNearMissClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One adaptive near miss claim, with what the planners need to act on it. */
data class AdaptiveNearMissClaim(
    var code: String,
    var name: String,
    var endsAt: Int,
    var dock: String,
    var budget: Double,
    var externalRef: String,
    var approved: Boolean,
    var channel: String,
    var segment: String,
    var etaMinutes: Int,
    var status: AdaptiveNearMissClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveNearMissClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveNearMissClaimStatus): Boolean {
        when (status) {
            AdaptiveNearMissClaimStatus.DRAFT -> return next == AdaptiveNearMissClaimStatus.PENDING || next == AdaptiveNearMissClaimStatus.CLOSED
            AdaptiveNearMissClaimStatus.PENDING -> return next == AdaptiveNearMissClaimStatus.SUSPENDED
            AdaptiveNearMissClaimStatus.ACTIVE -> return next == AdaptiveNearMissClaimStatus.SUSPENDED || next == AdaptiveNearMissClaimStatus.CLOSED
            AdaptiveNearMissClaimStatus.SUSPENDED -> return next == AdaptiveNearMissClaimStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: AdaptiveNearMissClaimStatus) {
        if (!canMoveTo(next)) {
            error("adaptive near miss claim $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"adaptive near miss claim"} $code"

    fun netValue(): Double = round((etaMinutes.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveNearMissClaim {
        return AdaptiveNearMissClaim(
            code = code,
            name = name,
            endsAt = endsAt,
            dock = dock,
            budget = budget,
            externalRef = externalRef,
            approved = approved,
            channel = channel,
            segment = segment,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a adaptive near miss claim in status draft. */
fun newAdaptiveNearMissClaim(code: String, name: String): AdaptiveNearMissClaim {
    return AdaptiveNearMissClaim(
        code = code,
        name = name,
        endsAt = 0,
        dock = "",
        budget = 0.0,
        externalRef = "",
        approved = false,
        channel = "",
        segment = "",
        etaMinutes = 0,
        status = AdaptiveNearMissClaimStatus.DRAFT,
    )
}
