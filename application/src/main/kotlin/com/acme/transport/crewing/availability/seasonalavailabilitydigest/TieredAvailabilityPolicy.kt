package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered availability policy is in its life cycle. */
enum class TieredAvailabilityPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A tiered availability policy. Its code is unique within a tenant. */
data class TieredAvailabilityPolicy(
    var code: String,
    var title: String,
    var retries: Int,
    var etaMinutes: Int,
    var fuelLiters: Double,
    var distanceKm: Double,
    var driverId: String,
    var ceiling: Double,
    var description: String,
    var ownerId: String,
    var locale: String,
    var status: TieredAvailabilityPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredAvailabilityPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredAvailabilityPolicyStatus): Boolean {
        when (status) {
            TieredAvailabilityPolicyStatus.PLANNED -> {
                when (next) {
                    TieredAvailabilityPolicyStatus.IN_PROGRESS -> return true
                }
                return false
            }
            TieredAvailabilityPolicyStatus.SCHEDULED -> return next == TieredAvailabilityPolicyStatus.IN_PROGRESS || next == TieredAvailabilityPolicyStatus.COMPLETED
            TieredAvailabilityPolicyStatus.IN_PROGRESS -> return next == TieredAvailabilityPolicyStatus.COMPLETED
            TieredAvailabilityPolicyStatus.COMPLETED -> return next == TieredAvailabilityPolicyStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TieredAvailabilityPolicyStatus) {
        if (!canMoveTo(next)) {
            error("tiered availability policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netTotal(): Double = round((fuelLiters + ceiling) * 100) / 100

    fun snapshot(): TieredAvailabilityPolicy {
        return TieredAvailabilityPolicy(
            code = code,
            title = title,
            retries = retries,
            etaMinutes = etaMinutes,
            fuelLiters = fuelLiters,
            distanceKm = distanceKm,
            driverId = driverId,
            ceiling = ceiling,
            description = description,
            ownerId = ownerId,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a tiered availability policy in status planned. */
fun newTieredAvailabilityPolicy(code: String, title: String): TieredAvailabilityPolicy {
    return TieredAvailabilityPolicy(
        code = code,
        title = title,
        retries = 0,
        etaMinutes = 0,
        fuelLiters = 0.0,
        distanceKm = 0.0,
        driverId = "",
        ceiling = 0.0,
        description = "",
        ownerId = "",
        locale = "",
        status = TieredAvailabilityPolicyStatus.PLANNED,
    )
}
