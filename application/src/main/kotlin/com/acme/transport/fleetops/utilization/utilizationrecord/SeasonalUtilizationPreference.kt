package com.acme.transport.fleetops.utilization.utilizationrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal utilization preference is in its life cycle. */
enum class SeasonalUtilizationPreferenceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal utilization preference as the utilization team keeps it. */
data class SeasonalUtilizationPreference(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var externalRef: String,
    var minimum: Double,
    var limit: Int,
    var odometer: Int,
    var attributes: MutableMap<String, String>,
    var driverId: String,
    var status: SeasonalUtilizationPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalUtilizationPreferenceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalUtilizationPreferenceStatus): Boolean {
        when (status) {
            SeasonalUtilizationPreferenceStatus.IDLE -> return next == SeasonalUtilizationPreferenceStatus.READY || next == SeasonalUtilizationPreferenceStatus.STOPPED
            SeasonalUtilizationPreferenceStatus.WARMING -> {
                when (next) {
                    SeasonalUtilizationPreferenceStatus.READY -> return true
                }
                return false
            }
            SeasonalUtilizationPreferenceStatus.READY -> return next == SeasonalUtilizationPreferenceStatus.DRAINING || next == SeasonalUtilizationPreferenceStatus.STOPPED
            SeasonalUtilizationPreferenceStatus.DRAINING -> {
                when (next) {
                    SeasonalUtilizationPreferenceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalUtilizationPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("seasonal utilization preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal utilization preference"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveValue(): Double = round(odometer.toDouble() * minimum * 100) / 100

    fun snapshot(): SeasonalUtilizationPreference {
        return SeasonalUtilizationPreference(
            code = code,
            name = name,
            distanceKm = distanceKm,
            externalRef = externalRef,
            minimum = minimum,
            limit = limit,
            odometer = odometer,
            attributes = attributes,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal utilization preference in status idle. */
fun newSeasonalUtilizationPreference(code: String, name: String): SeasonalUtilizationPreference {
    return SeasonalUtilizationPreference(
        code = code,
        name = name,
        distanceKm = 0.0,
        externalRef = "",
        minimum = 0.0,
        limit = 0,
        odometer = 0,
        attributes = mutableMapOf<String, String>(),
        driverId = "",
        status = SeasonalUtilizationPreferenceStatus.IDLE,
    )
}
