package com.acme.transport.network.zone.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped zone claim records of network's zone work, and the rules, stores, and calculations around them.

/** Where a capped zone claim is in its life cycle. */
enum class CappedZoneClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One capped zone claim, with what the planners need to act on it. */
data class CappedZoneClaim(
    var code: String,
    var name: String,
    var trailerId: String,
    var minimum: Double,
    var amount: Double,
    var score: Double,
    var currency: String,
    var status: CappedZoneClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedZoneClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedZoneClaimStatus): Boolean {
        when (status) {
            CappedZoneClaimStatus.CREATED -> return next == CappedZoneClaimStatus.VALIDATED || next == CappedZoneClaimStatus.RETIRED
            CappedZoneClaimStatus.VALIDATED -> return next == CappedZoneClaimStatus.RETIRED
            CappedZoneClaimStatus.PUBLISHED -> {
                when (next) {
                    CappedZoneClaimStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedZoneClaimStatus) {
        if (!canMoveTo(next)) {
            error("capped zone claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"capped zone claim"} $code"

    fun netValue(): Double = round((minimum + amount) * 100) / 100

    fun snapshot(): CappedZoneClaim {
        return CappedZoneClaim(
            code = code,
            name = name,
            trailerId = trailerId,
            minimum = minimum,
            amount = amount,
            score = score,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped zone claim in status created. */
fun newCappedZoneClaim(code: String, name: String): CappedZoneClaim {
    return CappedZoneClaim(
        code = code,
        name = name,
        trailerId = "",
        minimum = 0.0,
        amount = 0.0,
        score = 0.0,
        currency = "",
        status = CappedZoneClaimStatus.CREATED,
    )
}
