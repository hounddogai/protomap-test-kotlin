package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The attempt claim records of lastmile's attempt work, and the rules, stores, and calculations around them.

/** Where a attempt claim is in its life cycle. */
enum class AttemptClaimStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A attempt claim as the attempt team keeps it. */
data class AttemptClaim(
    var code: String,
    var name: String,
    var budget: Double,
    var margin: Double,
    var title: String,
    var stopCount: Int,
    var weight: Double,
    var discount: Double,
    var status: AttemptClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptClaimStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptClaimStatus): Boolean {
        when (status) {
            AttemptClaimStatus.PLANNED -> return next == AttemptClaimStatus.SCHEDULED || next == AttemptClaimStatus.COMPLETED
            AttemptClaimStatus.SCHEDULED -> {
                when (next) {
                    AttemptClaimStatus.COMPLETED -> return true
                }
                return false
            }
            AttemptClaimStatus.IN_PROGRESS -> {
                when (next) {
                    AttemptClaimStatus.COMPLETED -> return true
                }
                return false
            }
            AttemptClaimStatus.COMPLETED -> return next == AttemptClaimStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AttemptClaimStatus) {
        if (!canMoveTo(next)) {
            error("attempt claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun adjustedTotal(): Double = round(stopCount.toDouble() * weight * 100) / 100

    fun snapshot(): AttemptClaim {
        return AttemptClaim(
            code = code,
            name = name,
            budget = budget,
            margin = margin,
            title = title,
            stopCount = stopCount,
            weight = weight,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a attempt claim in status planned. */
fun newAttemptClaim(code: String, name: String): AttemptClaim {
    return AttemptClaim(
        code = code,
        name = name,
        budget = 0.0,
        margin = 0.0,
        title = "",
        stopCount = 0,
        weight = 0.0,
        discount = 0.0,
        status = AttemptClaimStatus.PLANNED,
    )
}
