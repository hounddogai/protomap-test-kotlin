package com.acme.transport.emissions.evcharge.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted ev charge claim is in its life cycle. */
enum class WeightedEvChargeClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted ev charge claim. Its code is unique within a tenant. */
data class WeightedEvChargeClaim(
    var code: String,
    var label: String,
    var version: Int,
    var priority: Int,
    var timezone: String,
    var region: String,
    var vehicleId: String,
    var summary: String,
    var distanceKm: Double,
    var minimum: Double,
    var status: WeightedEvChargeClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedEvChargeClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedEvChargeClaimStatus): Boolean {
        when (status) {
            WeightedEvChargeClaimStatus.OPEN -> return next == WeightedEvChargeClaimStatus.RESOLVED || next == WeightedEvChargeClaimStatus.REOPENED
            WeightedEvChargeClaimStatus.IN_REVIEW -> return next == WeightedEvChargeClaimStatus.RESOLVED
            WeightedEvChargeClaimStatus.RESOLVED -> return next == WeightedEvChargeClaimStatus.ARCHIVED
            WeightedEvChargeClaimStatus.REOPENED -> return next == WeightedEvChargeClaimStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: WeightedEvChargeClaimStatus) {
        if (!canMoveTo(next)) {
            error("weighted ev charge claim $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"weighted ev charge claim"} $code"

    fun netExposure(): Double = round(version.toDouble() * minimum * 100) / 100

    fun snapshot(): WeightedEvChargeClaim {
        return WeightedEvChargeClaim(
            code = code,
            label = label,
            version = version,
            priority = priority,
            timezone = timezone,
            region = region,
            vehicleId = vehicleId,
            summary = summary,
            distanceKm = distanceKm,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a weighted ev charge claim in status open. */
fun newWeightedEvChargeClaim(code: String, label: String): WeightedEvChargeClaim {
    return WeightedEvChargeClaim(
        code = code,
        label = label,
        version = 0,
        priority = 0,
        timezone = "",
        region = "",
        vehicleId = "",
        summary = "",
        distanceKm = 0.0,
        minimum = 0.0,
        status = WeightedEvChargeClaimStatus.OPEN,
    )
}
