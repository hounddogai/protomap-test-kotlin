package com.acme.transport.linehaul.backhaul.backhaulquota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal backhaul quota records of linehaul's backhaul work, and the rules, stores, and calculations
// around them.

/** Where a seasonal backhaul quota is in its life cycle. */
enum class SeasonalBackhaulQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One seasonal backhaul quota, with what the planners need to act on it. */
data class SeasonalBackhaulQuota(
    var code: String,
    var label: String,
    var driverId: String,
    var floorValue: Double,
    var distanceKm: Double,
    var vehicleId: String,
    var unit: String,
    var status: SeasonalBackhaulQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalBackhaulQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalBackhaulQuotaStatus): Boolean {
        when (status) {
            SeasonalBackhaulQuotaStatus.OPEN -> {
                when (next) {
                    SeasonalBackhaulQuotaStatus.REOPENED -> return true
                }
                return false
            }
            SeasonalBackhaulQuotaStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalBackhaulQuotaStatus.RESOLVED, SeasonalBackhaulQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalBackhaulQuotaStatus.RESOLVED -> return next == SeasonalBackhaulQuotaStatus.REOPENED || next == SeasonalBackhaulQuotaStatus.ARCHIVED
            SeasonalBackhaulQuotaStatus.REOPENED -> return next == SeasonalBackhaulQuotaStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: SeasonalBackhaulQuotaStatus) {
        if (!canMoveTo(next)) {
            error("seasonal backhaul quota $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal backhaul quota"} $code"

    fun weightedVolume(): Double = round((distanceKm - floorValue) * 100) / 100

    fun snapshot(): SeasonalBackhaulQuota {
        return SeasonalBackhaulQuota(
            code = code,
            label = label,
            driverId = driverId,
            floorValue = floorValue,
            distanceKm = distanceKm,
            vehicleId = vehicleId,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a seasonal backhaul quota in status open. */
fun newSeasonalBackhaulQuota(code: String, label: String): SeasonalBackhaulQuota {
    return SeasonalBackhaulQuota(
        code = code,
        label = label,
        driverId = "",
        floorValue = 0.0,
        distanceKm = 0.0,
        vehicleId = "",
        unit = "",
        status = SeasonalBackhaulQuotaStatus.OPEN,
    )
}
