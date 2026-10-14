package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict incident claim records of safety's incident work, and the rules, stores, and calculations around
// them.

/** Where a strict incident claim is in its life cycle. */
enum class StrictIncidentClaimStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict incident claim. Its code is unique within a tenant. */
data class StrictIncidentClaim(
    var code: String,
    var title: String,
    var etaMinutes: Int,
    var checksum: String,
    var distanceKm: Double,
    var attempts: Int,
    var status: StrictIncidentClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictIncidentClaimStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictIncidentClaimStatus): Boolean {
        when (status) {
            StrictIncidentClaimStatus.DRAFT -> {
                when (next) {
                    StrictIncidentClaimStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictIncidentClaimStatus.PENDING -> {
                when (next) {
                    StrictIncidentClaimStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictIncidentClaimStatus.ACTIVE -> return next == StrictIncidentClaimStatus.SUSPENDED || next == StrictIncidentClaimStatus.CLOSED
            StrictIncidentClaimStatus.SUSPENDED -> {
                when (next) {
                    StrictIncidentClaimStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictIncidentClaimStatus) {
        if (!canMoveTo(next)) {
            error("strict incident claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun weightedLoad(): Double = round((etaMinutes.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): StrictIncidentClaim {
        return StrictIncidentClaim(
            code = code,
            title = title,
            etaMinutes = etaMinutes,
            checksum = checksum,
            distanceKm = distanceKm,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict incident claim in status draft. */
fun newStrictIncidentClaim(code: String, title: String): StrictIncidentClaim {
    return StrictIncidentClaim(
        code = code,
        title = title,
        etaMinutes = 0,
        checksum = "",
        distanceKm = 0.0,
        attempts = 0,
        status = StrictIncidentClaimStatus.DRAFT,
    )
}
