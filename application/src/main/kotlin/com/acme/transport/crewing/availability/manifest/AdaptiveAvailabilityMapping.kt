package com.acme.transport.crewing.availability.manifest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive availability mapping is in its life cycle. */
enum class AdaptiveAvailabilityMappingStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A adaptive availability mapping as the availability team keeps it. */
data class AdaptiveAvailabilityMapping(
    var code: String,
    var title: String,
    var vehicleId: String,
    var durationMinutes: Int,
    var attempts: Int,
    var status: AdaptiveAvailabilityMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAvailabilityMappingStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAvailabilityMappingStatus): Boolean {
        when (status) {
            AdaptiveAvailabilityMappingStatus.REQUESTED -> {
                when (next) {
                    AdaptiveAvailabilityMappingStatus.REVOKED, AdaptiveAvailabilityMappingStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveAvailabilityMappingStatus.GRANTED -> return next == AdaptiveAvailabilityMappingStatus.REVOKED || next == AdaptiveAvailabilityMappingStatus.EXPIRED
            AdaptiveAvailabilityMappingStatus.REVOKED -> return next == AdaptiveAvailabilityMappingStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AdaptiveAvailabilityMappingStatus) {
        if (!canMoveTo(next)) {
            error("adaptive availability mapping $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun projectedLoad(): Double = round(attempts.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): AdaptiveAvailabilityMapping {
        return AdaptiveAvailabilityMapping(
            code = code,
            title = title,
            vehicleId = vehicleId,
            durationMinutes = durationMinutes,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive availability mapping in status requested. */
fun newAdaptiveAvailabilityMapping(code: String, title: String): AdaptiveAvailabilityMapping {
    return AdaptiveAvailabilityMapping(
        code = code,
        title = title,
        vehicleId = "",
        durationMinutes = 0,
        attempts = 0,
        status = AdaptiveAvailabilityMappingStatus.REQUESTED,
    )
}
