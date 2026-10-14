package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary trailer claim is in its life cycle. */
enum class PrimaryTrailerClaimStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One primary trailer claim, with what the planners need to act on it. */
data class PrimaryTrailerClaim(
    var code: String,
    var label: String,
    var quantity: Int,
    var margin: Double,
    var budget: Double,
    var threshold: Double,
    var revision: Int,
    var reviewer: String,
    var maximum: Double,
    var discount: Double,
    var segment: String,
    var status: PrimaryTrailerClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryTrailerClaimStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryTrailerClaimStatus): Boolean {
        when (status) {
            PrimaryTrailerClaimStatus.PROPOSED -> return next == PrimaryTrailerClaimStatus.WITHDRAWN
            PrimaryTrailerClaimStatus.APPROVED -> return next == PrimaryTrailerClaimStatus.REJECTED
            PrimaryTrailerClaimStatus.REJECTED -> {
                when (next) {
                    PrimaryTrailerClaimStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryTrailerClaimStatus) {
        if (!canMoveTo(next)) {
            error("primary trailer claim $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"primary trailer claim"} $code"

    fun projectedVolume(): Double = round((quantity.toDouble() - revision.toDouble()) * 100) / 100

    fun snapshot(): PrimaryTrailerClaim {
        return PrimaryTrailerClaim(
            code = code,
            label = label,
            quantity = quantity,
            margin = margin,
            budget = budget,
            threshold = threshold,
            revision = revision,
            reviewer = reviewer,
            maximum = maximum,
            discount = discount,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary trailer claim in status proposed. */
fun newPrimaryTrailerClaim(code: String, label: String): PrimaryTrailerClaim {
    return PrimaryTrailerClaim(
        code = code,
        label = label,
        quantity = 0,
        margin = 0.0,
        budget = 0.0,
        threshold = 0.0,
        revision = 0,
        reviewer = "",
        maximum = 0.0,
        discount = 0.0,
        segment = "",
        status = PrimaryTrailerClaimStatus.PROPOSED,
    )
}
