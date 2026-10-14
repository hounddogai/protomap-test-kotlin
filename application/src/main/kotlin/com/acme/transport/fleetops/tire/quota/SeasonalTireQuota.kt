package com.acme.transport.fleetops.tire.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal tire quota records of fleetops's tire work, and the rules, stores, and calculations around
// them.

/** Where a seasonal tire quota is in its life cycle. */
enum class SeasonalTireQuotaStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal tire quota. Its code is unique within a tenant. */
data class SeasonalTireQuota(
    var code: String,
    var name: String,
    var floorValue: Double,
    var etaMinutes: Int,
    var expiresAt: Int,
    var odometer: Int,
    var status: SeasonalTireQuotaStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTireQuotaStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTireQuotaStatus): Boolean {
        when (status) {
            SeasonalTireQuotaStatus.OPEN -> {
                when (next) {
                    SeasonalTireQuotaStatus.RESOLVED -> return true
                }
                return false
            }
            SeasonalTireQuotaStatus.IN_REVIEW -> return next == SeasonalTireQuotaStatus.RESOLVED || next == SeasonalTireQuotaStatus.REOPENED
            SeasonalTireQuotaStatus.RESOLVED -> return next == SeasonalTireQuotaStatus.REOPENED || next == SeasonalTireQuotaStatus.ARCHIVED
            SeasonalTireQuotaStatus.REOPENED -> {
                when (next) {
                    SeasonalTireQuotaStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTireQuotaStatus) {
        if (!canMoveTo(next)) {
            error("seasonal tire quota $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal tire quota"} $code"

    fun weightedTotal(): Double = round(odometer.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): SeasonalTireQuota {
        return SeasonalTireQuota(
            code = code,
            name = name,
            floorValue = floorValue,
            etaMinutes = etaMinutes,
            expiresAt = expiresAt,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal tire quota in status open. */
fun newSeasonalTireQuota(code: String, name: String): SeasonalTireQuota {
    return SeasonalTireQuota(
        code = code,
        name = name,
        floorValue = 0.0,
        etaMinutes = 0,
        expiresAt = 0,
        odometer = 0,
        status = SeasonalTireQuotaStatus.OPEN,
    )
}
