package com.acme.transport.emissions.routeoffset.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal route offset claim is in its life cycle. */
enum class SeasonalRouteOffsetClaimStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a seasonal route offset claim and its current status. */
data class SeasonalRouteOffsetClaim(
    var code: String,
    var label: String,
    var distanceKm: Double,
    var depot: String,
    var floorValue: Double,
    var createdAt: Int,
    var stopCount: Int,
    var capacity: Int,
    var name: String,
    var contactEmail: String,
    var attempts: Int,
    var status: SeasonalRouteOffsetClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRouteOffsetClaimStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRouteOffsetClaimStatus): Boolean {
        when (status) {
            SeasonalRouteOffsetClaimStatus.REQUESTED -> {
                when (next) {
                    SeasonalRouteOffsetClaimStatus.GRANTED, SeasonalRouteOffsetClaimStatus.REVOKED -> return true
                }
                return false
            }
            SeasonalRouteOffsetClaimStatus.GRANTED -> {
                when (next) {
                    SeasonalRouteOffsetClaimStatus.REVOKED -> return true
                }
                return false
            }
            SeasonalRouteOffsetClaimStatus.REVOKED -> return next == SeasonalRouteOffsetClaimStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: SeasonalRouteOffsetClaimStatus) {
        if (!canMoveTo(next)) {
            error("seasonal route offset claim $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveValue(): Double = round((attempts.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): SeasonalRouteOffsetClaim {
        return SeasonalRouteOffsetClaim(
            code = code,
            label = label,
            distanceKm = distanceKm,
            depot = depot,
            floorValue = floorValue,
            createdAt = createdAt,
            stopCount = stopCount,
            capacity = capacity,
            name = name,
            contactEmail = contactEmail,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a seasonal route offset claim in status requested. */
fun newSeasonalRouteOffsetClaim(code: String, label: String): SeasonalRouteOffsetClaim {
    return SeasonalRouteOffsetClaim(
        code = code,
        label = label,
        distanceKm = 0.0,
        depot = "",
        floorValue = 0.0,
        createdAt = 0,
        stopCount = 0,
        capacity = 0,
        name = "",
        contactEmail = "",
        attempts = 0,
        status = SeasonalRouteOffsetClaimStatus.REQUESTED,
    )
}
