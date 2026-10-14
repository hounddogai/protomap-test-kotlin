package com.acme.transport.network.zone.profile

import com.acme.transport.kit.text.normalize

// The legacy zone profile records of network's zone work, and the rules, stores, and calculations around
// them.

/** Where a legacy zone profile is in its life cycle. */
enum class LegacyZoneProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A legacy zone profile as the zone team keeps it. */
data class LegacyZoneProfile(
    var code: String,
    var label: String,
    var depot: String,
    var vehicleId: String,
    var weight: Double,
    var status: LegacyZoneProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyZoneProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyZoneProfileStatus): Boolean {
        when (status) {
            LegacyZoneProfileStatus.DRAFT -> return next == LegacyZoneProfileStatus.CLOSED
            LegacyZoneProfileStatus.PENDING -> {
                when (next) {
                    LegacyZoneProfileStatus.SUSPENDED -> return true
                }
                return false
            }
            LegacyZoneProfileStatus.ACTIVE -> {
                when (next) {
                    LegacyZoneProfileStatus.SUSPENDED, LegacyZoneProfileStatus.CLOSED -> return true
                }
                return false
            }
            LegacyZoneProfileStatus.SUSPENDED -> return next == LegacyZoneProfileStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LegacyZoneProfileStatus) {
        if (!canMoveTo(next)) {
            error("legacy zone profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = weight

    fun snapshot(): LegacyZoneProfile {
        return LegacyZoneProfile(
            code = code,
            label = label,
            depot = depot,
            vehicleId = vehicleId,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a legacy zone profile in status draft. */
fun newLegacyZoneProfile(code: String, label: String): LegacyZoneProfile {
    return LegacyZoneProfile(
        code = code,
        label = label,
        depot = "",
        vehicleId = "",
        weight = 0.0,
        status = LegacyZoneProfileStatus.DRAFT,
    )
}
