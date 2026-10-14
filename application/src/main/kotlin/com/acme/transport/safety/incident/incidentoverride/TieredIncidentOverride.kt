package com.acme.transport.safety.incident.incidentoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The tiered incident override records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a tiered incident override is in its life cycle. */
enum class TieredIncidentOverrideStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One tiered incident override, with what the planners need to act on it. */
data class TieredIncidentOverride(
    var code: String,
    var name: String,
    var country: String,
    var vehicleId: String,
    var approved: Boolean,
    var fuelLiters: Double,
    var visible: Boolean,
    var reviewer: String,
    var source: String,
    var graceDays: Int,
    var status: TieredIncidentOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredIncidentOverrideStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredIncidentOverrideStatus): Boolean {
        when (status) {
            TieredIncidentOverrideStatus.OPEN -> return next == TieredIncidentOverrideStatus.IN_REVIEW || next == TieredIncidentOverrideStatus.RESOLVED
            TieredIncidentOverrideStatus.IN_REVIEW -> return next == TieredIncidentOverrideStatus.RESOLVED
            TieredIncidentOverrideStatus.RESOLVED -> return next == TieredIncidentOverrideStatus.REOPENED || next == TieredIncidentOverrideStatus.ARCHIVED
            TieredIncidentOverrideStatus.REOPENED -> {
                when (next) {
                    TieredIncidentOverrideStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredIncidentOverrideStatus) {
        if (!canMoveTo(next)) {
            error("tiered incident override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered incident override"} $code"

    fun netVolume(): Double = round((graceDays.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): TieredIncidentOverride {
        return TieredIncidentOverride(
            code = code,
            name = name,
            country = country,
            vehicleId = vehicleId,
            approved = approved,
            fuelLiters = fuelLiters,
            visible = visible,
            reviewer = reviewer,
            source = source,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a tiered incident override in status open. */
fun newTieredIncidentOverride(code: String, name: String): TieredIncidentOverride {
    return TieredIncidentOverride(
        code = code,
        name = name,
        country = "",
        vehicleId = "",
        approved = false,
        fuelLiters = 0.0,
        visible = false,
        reviewer = "",
        source = "",
        graceDays = 0,
        status = TieredIncidentOverrideStatus.OPEN,
    )
}
