package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled availability profile is in its life cycle. */
enum class ScheduledAvailabilityProfileStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a scheduled availability profile and its current status. */
data class ScheduledAvailabilityProfile(
    var code: String,
    var label: String,
    var region: String,
    var driverId: String,
    var maximum: Double,
    var updatedAt: Int,
    var attempts: Int,
    var status: ScheduledAvailabilityProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAvailabilityProfileStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAvailabilityProfileStatus): Boolean {
        when (status) {
            ScheduledAvailabilityProfileStatus.CREATED -> return next == ScheduledAvailabilityProfileStatus.VALIDATED || next == ScheduledAvailabilityProfileStatus.RETIRED
            ScheduledAvailabilityProfileStatus.VALIDATED -> {
                when (next) {
                    ScheduledAvailabilityProfileStatus.PUBLISHED -> return true
                }
                return false
            }
            ScheduledAvailabilityProfileStatus.PUBLISHED -> return next == ScheduledAvailabilityProfileStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledAvailabilityProfileStatus) {
        if (!canMoveTo(next)) {
            error("scheduled availability profile $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedLoad(): Double = round(updatedAt.toDouble() * maximum * 100) / 100

    fun snapshot(): ScheduledAvailabilityProfile {
        return ScheduledAvailabilityProfile(
            code = code,
            label = label,
            region = region,
            driverId = driverId,
            maximum = maximum,
            updatedAt = updatedAt,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled availability profile in status created. */
fun newScheduledAvailabilityProfile(code: String, label: String): ScheduledAvailabilityProfile {
    return ScheduledAvailabilityProfile(
        code = code,
        label = label,
        region = "",
        driverId = "",
        maximum = 0.0,
        updatedAt = 0,
        attempts = 0,
        status = ScheduledAvailabilityProfileStatus.CREATED,
    )
}
