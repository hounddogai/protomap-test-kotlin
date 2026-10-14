package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy zone mapping records of network's zone work, and the rules, stores, and calculations around
// them.

/** Where a legacy zone mapping is in its life cycle. */
enum class LegacyZoneMappingStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A legacy zone mapping as the zone team keeps it. */
data class LegacyZoneMapping(
    var code: String,
    var label: String,
    var country: String,
    var timezone: String,
    var odometer: Int,
    var cost: Double,
    var attempts: Int,
    var reviewer: String,
    var unit: String,
    var locale: String,
    var status: LegacyZoneMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyZoneMappingStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyZoneMappingStatus): Boolean {
        when (status) {
            LegacyZoneMappingStatus.PLANNED -> return next == LegacyZoneMappingStatus.COMPLETED
            LegacyZoneMappingStatus.SCHEDULED -> return next == LegacyZoneMappingStatus.IN_PROGRESS || next == LegacyZoneMappingStatus.COMPLETED
            LegacyZoneMappingStatus.IN_PROGRESS -> return next == LegacyZoneMappingStatus.COMPLETED
            LegacyZoneMappingStatus.COMPLETED -> return next == LegacyZoneMappingStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: LegacyZoneMappingStatus) {
        if (!canMoveTo(next)) {
            error("legacy zone mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netVolume(): Double = round((odometer.toDouble() - cost) * 100) / 100

    fun snapshot(): LegacyZoneMapping {
        return LegacyZoneMapping(
            code = code,
            label = label,
            country = country,
            timezone = timezone,
            odometer = odometer,
            cost = cost,
            attempts = attempts,
            reviewer = reviewer,
            unit = unit,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a legacy zone mapping in status planned. */
fun newLegacyZoneMapping(code: String, label: String): LegacyZoneMapping {
    return LegacyZoneMapping(
        code = code,
        label = label,
        country = "",
        timezone = "",
        odometer = 0,
        cost = 0.0,
        attempts = 0,
        reviewer = "",
        unit = "",
        locale = "",
        status = LegacyZoneMappingStatus.PLANNED,
    )
}
