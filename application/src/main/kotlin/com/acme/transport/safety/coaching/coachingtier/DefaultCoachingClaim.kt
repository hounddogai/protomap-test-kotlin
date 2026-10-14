package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a default coaching claim is in its life cycle. */
enum class DefaultCoachingClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A default coaching claim as the coaching team keeps it. */
data class DefaultCoachingClaim(
    var code: String,
    var title: String,
    var quantity: Int,
    var loadKg: Double,
    var summary: String,
    var sequence: Int,
    var status: DefaultCoachingClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultCoachingClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultCoachingClaimStatus): Boolean {
        when (status) {
            DefaultCoachingClaimStatus.OPEN -> return next == DefaultCoachingClaimStatus.IN_REVIEW || next == DefaultCoachingClaimStatus.REOPENED
            DefaultCoachingClaimStatus.IN_REVIEW -> return next == DefaultCoachingClaimStatus.RESOLVED || next == DefaultCoachingClaimStatus.REOPENED
            DefaultCoachingClaimStatus.RESOLVED -> {
                when (next) {
                    DefaultCoachingClaimStatus.REOPENED, DefaultCoachingClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultCoachingClaimStatus.REOPENED -> {
                when (next) {
                    DefaultCoachingClaimStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultCoachingClaimStatus) {
        if (!canMoveTo(next)) {
            error("default coaching claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"default coaching claim"} $code"

    fun adjustedTotal(): Double = round(sequence.toDouble() * loadKg * 100) / 100

    fun snapshot(): DefaultCoachingClaim {
        return DefaultCoachingClaim(
            code = code,
            title = title,
            quantity = quantity,
            loadKg = loadKg,
            summary = summary,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a default coaching claim in status open. */
fun newDefaultCoachingClaim(code: String, title: String): DefaultCoachingClaim {
    return DefaultCoachingClaim(
        code = code,
        title = title,
        quantity = 0,
        loadKg = 0.0,
        summary = "",
        sequence = 0,
        status = DefaultCoachingClaimStatus.OPEN,
    )
}
