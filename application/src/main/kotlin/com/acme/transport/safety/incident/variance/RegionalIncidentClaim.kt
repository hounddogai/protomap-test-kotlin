package com.acme.transport.safety.incident.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional incident claim is in its life cycle. */
enum class RegionalIncidentClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a regional incident claim and its current status. */
data class RegionalIncidentClaim(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var fuelLiters: Double,
    var region: String,
    var trailerId: String,
    var weight: Double,
    var checksum: String,
    var routeCode: String,
    var status: RegionalIncidentClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalIncidentClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalIncidentClaimStatus): Boolean {
        when (status) {
            RegionalIncidentClaimStatus.OPEN -> {
                when (next) {
                    RegionalIncidentClaimStatus.RESOLVED, RegionalIncidentClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            RegionalIncidentClaimStatus.IN_REVIEW -> {
                when (next) {
                    RegionalIncidentClaimStatus.REOPENED -> return true
                }
                return false
            }
            RegionalIncidentClaimStatus.RESOLVED -> return next == RegionalIncidentClaimStatus.REOPENED || next == RegionalIncidentClaimStatus.ARCHIVED
            RegionalIncidentClaimStatus.REOPENED -> return next == RegionalIncidentClaimStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RegionalIncidentClaimStatus) {
        if (!canMoveTo(next)) {
            error("regional incident claim $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round((weight + fuelLiters) * 100) / 100

    fun snapshot(): RegionalIncidentClaim {
        return RegionalIncidentClaim(
            code = code,
            name = name,
            updatedAt = updatedAt,
            fuelLiters = fuelLiters,
            region = region,
            trailerId = trailerId,
            weight = weight,
            checksum = checksum,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a regional incident claim in status open. */
fun newRegionalIncidentClaim(code: String, name: String): RegionalIncidentClaim {
    return RegionalIncidentClaim(
        code = code,
        name = name,
        updatedAt = 0,
        fuelLiters = 0.0,
        region = "",
        trailerId = "",
        weight = 0.0,
        checksum = "",
        routeCode = "",
        status = RegionalIncidentClaimStatus.OPEN,
    )
}
