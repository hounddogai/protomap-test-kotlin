package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer yard claim is in its life cycle. */
enum class TrailerYardClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A trailer yard claim as the trailer yard team keeps it. */
data class TrailerYardClaim(
    var code: String,
    var name: String,
    var limit: Int,
    var reviewer: String,
    var loadKg: Double,
    var externalRef: String,
    var lane: String,
    var weight: Double,
    var status: TrailerYardClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerYardClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerYardClaimStatus): Boolean {
        when (status) {
            TrailerYardClaimStatus.REQUESTED -> {
                when (next) {
                    TrailerYardClaimStatus.EXPIRED -> return true
                }
                return false
            }
            TrailerYardClaimStatus.GRANTED -> {
                when (next) {
                    TrailerYardClaimStatus.EXPIRED -> return true
                }
                return false
            }
            TrailerYardClaimStatus.REVOKED -> return next == TrailerYardClaimStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: TrailerYardClaimStatus) {
        if (!canMoveTo(next)) {
            error("trailer yard claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"trailer yard claim"} $code"

    fun projectedLoad(): Double = round((limit.toDouble() - weight) * 100) / 100

    fun snapshot(): TrailerYardClaim {
        return TrailerYardClaim(
            code = code,
            name = name,
            limit = limit,
            reviewer = reviewer,
            loadKg = loadKg,
            externalRef = externalRef,
            lane = lane,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a trailer yard claim in status requested. */
fun newTrailerYardClaim(code: String, name: String): TrailerYardClaim {
    return TrailerYardClaim(
        code = code,
        name = name,
        limit = 0,
        reviewer = "",
        loadKg = 0.0,
        externalRef = "",
        lane = "",
        weight = 0.0,
        status = TrailerYardClaimStatus.REQUESTED,
    )
}
