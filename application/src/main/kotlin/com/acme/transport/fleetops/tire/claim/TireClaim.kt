package com.acme.transport.fleetops.tire.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tire claim records of fleetops's tire work, and the rules, stores, and calculations around them.

/** Where a tire claim is in its life cycle. */
enum class TireClaimStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tire claim. Its code is unique within a tenant. */
data class TireClaim(
    var code: String,
    var label: String,
    var trailerId: String,
    var amount: Double,
    var source: String,
    var budget: Double,
    var odometer: Int,
    var stopCount: Int,
    var durationMinutes: Int,
    var startsAt: Int,
    var status: TireClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireClaimStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TireClaimStatus): Boolean {
        when (status) {
            TireClaimStatus.PROPOSED -> {
                when (next) {
                    TireClaimStatus.APPROVED -> return true
                }
                return false
            }
            TireClaimStatus.APPROVED -> return next == TireClaimStatus.REJECTED || next == TireClaimStatus.WITHDRAWN
            TireClaimStatus.REJECTED -> return next == TireClaimStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TireClaimStatus) {
        if (!canMoveTo(next)) {
            error("tire claim $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveExposure(): Double = round(durationMinutes.toDouble() * amount * 100) / 100

    fun snapshot(): TireClaim {
        return TireClaim(
            code = code,
            label = label,
            trailerId = trailerId,
            amount = amount,
            source = source,
            budget = budget,
            odometer = odometer,
            stopCount = stopCount,
            durationMinutes = durationMinutes,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a tire claim in status proposed. */
fun newTireClaim(code: String, label: String): TireClaim {
    return TireClaim(
        code = code,
        label = label,
        trailerId = "",
        amount = 0.0,
        source = "",
        budget = 0.0,
        odometer = 0,
        stopCount = 0,
        durationMinutes = 0,
        startsAt = 0,
        status = TireClaimStatus.PROPOSED,
    )
}
