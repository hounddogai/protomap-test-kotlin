package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.kit.text.normalize

/** Where a trailer claim is in its life cycle. */
enum class TrailerClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A trailer claim as the trailer team keeps it. */
data class TrailerClaim(
    var code: String,
    var label: String,
    var trailerId: String,
    var attempts: Int,
    var checksum: String,
    var status: TrailerClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerClaimStatus): Boolean {
        when (status) {
            TrailerClaimStatus.REQUESTED -> {
                when (next) {
                    TrailerClaimStatus.GRANTED, TrailerClaimStatus.REVOKED -> return true
                }
                return false
            }
            TrailerClaimStatus.GRANTED -> {
                when (next) {
                    TrailerClaimStatus.REVOKED, TrailerClaimStatus.EXPIRED -> return true
                }
                return false
            }
            TrailerClaimStatus.REVOKED -> {
                when (next) {
                    TrailerClaimStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerClaimStatus) {
        if (!canMoveTo(next)) {
            error("trailer claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): TrailerClaim {
        return TrailerClaim(
            code = code,
            label = label,
            trailerId = trailerId,
            attempts = attempts,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a trailer claim in status requested. */
fun newTrailerClaim(code: String, label: String): TrailerClaim {
    return TrailerClaim(
        code = code,
        label = label,
        trailerId = "",
        attempts = 0,
        checksum = "",
        status = TrailerClaimStatus.REQUESTED,
    )
}
