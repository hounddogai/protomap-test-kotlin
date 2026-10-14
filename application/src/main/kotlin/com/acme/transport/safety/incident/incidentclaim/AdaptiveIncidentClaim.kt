package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.text.normalize

// The adaptive incident claim records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a adaptive incident claim is in its life cycle. */
enum class AdaptiveIncidentClaimStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a adaptive incident claim and its current status. */
data class AdaptiveIncidentClaim(
    var code: String,
    var name: String,
    var country: String,
    var unit: String,
    var etaMinutes: Int,
    var status: AdaptiveIncidentClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveIncidentClaimStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveIncidentClaimStatus): Boolean {
        when (status) {
            AdaptiveIncidentClaimStatus.IDLE -> {
                when (next) {
                    AdaptiveIncidentClaimStatus.WARMING, AdaptiveIncidentClaimStatus.STOPPED -> return true
                }
                return false
            }
            AdaptiveIncidentClaimStatus.WARMING -> {
                when (next) {
                    AdaptiveIncidentClaimStatus.READY, AdaptiveIncidentClaimStatus.DRAINING -> return true
                }
                return false
            }
            AdaptiveIncidentClaimStatus.READY -> return next == AdaptiveIncidentClaimStatus.STOPPED
            AdaptiveIncidentClaimStatus.DRAINING -> {
                when (next) {
                    AdaptiveIncidentClaimStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveIncidentClaimStatus) {
        if (!canMoveTo(next)) {
            error("adaptive incident claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = etaMinutes.toDouble()

    fun snapshot(): AdaptiveIncidentClaim {
        return AdaptiveIncidentClaim(
            code = code,
            name = name,
            country = country,
            unit = unit,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive incident claim in status idle. */
fun newAdaptiveIncidentClaim(code: String, name: String): AdaptiveIncidentClaim {
    return AdaptiveIncidentClaim(
        code = code,
        name = name,
        country = "",
        unit = "",
        etaMinutes = 0,
        status = AdaptiveIncidentClaimStatus.IDLE,
    )
}
