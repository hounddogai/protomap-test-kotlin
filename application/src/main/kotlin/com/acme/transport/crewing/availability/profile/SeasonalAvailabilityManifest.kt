package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal availability manifest is in its life cycle. */
enum class SeasonalAvailabilityManifestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A seasonal availability manifest as the availability team keeps it. */
data class SeasonalAvailabilityManifest(
    var code: String,
    var name: String,
    var label: String,
    var margin: Double,
    var version: Int,
    var phone: String,
    var revision: Int,
    var routeCode: String,
    var distanceKm: Double,
    var windowDays: Int,
    var reviewer: String,
    var status: SeasonalAvailabilityManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAvailabilityManifestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAvailabilityManifestStatus): Boolean {
        when (status) {
            SeasonalAvailabilityManifestStatus.PLANNED -> return next == SeasonalAvailabilityManifestStatus.SCHEDULED || next == SeasonalAvailabilityManifestStatus.COMPLETED
            SeasonalAvailabilityManifestStatus.SCHEDULED -> {
                when (next) {
                    SeasonalAvailabilityManifestStatus.IN_PROGRESS -> return true
                }
                return false
            }
            SeasonalAvailabilityManifestStatus.IN_PROGRESS -> return next == SeasonalAvailabilityManifestStatus.COMPLETED || next == SeasonalAvailabilityManifestStatus.CANCELLED
            SeasonalAvailabilityManifestStatus.COMPLETED -> {
                when (next) {
                    SeasonalAvailabilityManifestStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalAvailabilityManifestStatus) {
        if (!canMoveTo(next)) {
            error("seasonal availability manifest $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$name]"

    fun adjustedExposure(): Double = round(distanceKm * margin * 100) / 100

    fun snapshot(): SeasonalAvailabilityManifest {
        return SeasonalAvailabilityManifest(
            code = code,
            name = name,
            label = label,
            margin = margin,
            version = version,
            phone = phone,
            revision = revision,
            routeCode = routeCode,
            distanceKm = distanceKm,
            windowDays = windowDays,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal availability manifest in status planned. */
fun newSeasonalAvailabilityManifest(code: String, name: String): SeasonalAvailabilityManifest {
    return SeasonalAvailabilityManifest(
        code = code,
        name = name,
        label = "",
        margin = 0.0,
        version = 0,
        phone = "",
        revision = 0,
        routeCode = "",
        distanceKm = 0.0,
        windowDays = 0,
        reviewer = "",
        status = SeasonalAvailabilityManifestStatus.PLANNED,
    )
}
