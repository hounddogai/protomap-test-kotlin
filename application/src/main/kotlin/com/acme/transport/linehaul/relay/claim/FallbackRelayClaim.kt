package com.acme.transport.linehaul.relay.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback relay claim records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a fallback relay claim is in its life cycle. */
enum class FallbackRelayClaimStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback relay claim as the relay team keeps it. */
data class FallbackRelayClaim(
    var code: String,
    var name: String,
    var phone: String,
    var minimum: Double,
    var graceDays: Int,
    var weight: Double,
    var title: String,
    var status: FallbackRelayClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRelayClaimStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRelayClaimStatus): Boolean {
        when (status) {
            FallbackRelayClaimStatus.NEW -> return next == FallbackRelayClaimStatus.DONE || next == FallbackRelayClaimStatus.FAILED
            FallbackRelayClaimStatus.QUEUED -> {
                when (next) {
                    FallbackRelayClaimStatus.FAILED -> return true
                }
                return false
            }
            FallbackRelayClaimStatus.RUNNING -> return next == FallbackRelayClaimStatus.DONE
            FallbackRelayClaimStatus.DONE -> return next == FallbackRelayClaimStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackRelayClaimStatus) {
        if (!canMoveTo(next)) {
            error("fallback relay claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedTotal(): Double = round(graceDays.toDouble() * weight * 100) / 100

    fun snapshot(): FallbackRelayClaim {
        return FallbackRelayClaim(
            code = code,
            name = name,
            phone = phone,
            minimum = minimum,
            graceDays = graceDays,
            weight = weight,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a fallback relay claim in status new. */
fun newFallbackRelayClaim(code: String, name: String): FallbackRelayClaim {
    return FallbackRelayClaim(
        code = code,
        name = name,
        phone = "",
        minimum = 0.0,
        graceDays = 0,
        weight = 0.0,
        title = "",
        status = FallbackRelayClaimStatus.NEW,
    )
}
