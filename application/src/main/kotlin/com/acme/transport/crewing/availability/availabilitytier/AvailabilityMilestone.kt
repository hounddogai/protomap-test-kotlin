package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a availability milestone is in its life cycle. */
enum class AvailabilityMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One availability milestone, with what the planners need to act on it. */
data class AvailabilityMilestone(
    var code: String,
    var title: String,
    var pallets: Int,
    var loadKg: Double,
    var retries: Int,
    var fuelLiters: Double,
    var segment: String,
    var locale: String,
    var externalRef: String,
    var currency: String,
    var status: AvailabilityMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityMilestoneStatus): Boolean {
        when (status) {
            AvailabilityMilestoneStatus.PLANNED -> {
                when (next) {
                    AvailabilityMilestoneStatus.COMPLETED -> return true
                }
                return false
            }
            AvailabilityMilestoneStatus.SCHEDULED -> return next == AvailabilityMilestoneStatus.COMPLETED
            AvailabilityMilestoneStatus.IN_PROGRESS -> {
                when (next) {
                    AvailabilityMilestoneStatus.COMPLETED, AvailabilityMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            AvailabilityMilestoneStatus.COMPLETED -> {
                when (next) {
                    AvailabilityMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("availability milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedExposure(): Double = round(fuelLiters * loadKg * 100) / 100

    fun snapshot(): AvailabilityMilestone {
        return AvailabilityMilestone(
            code = code,
            title = title,
            pallets = pallets,
            loadKg = loadKg,
            retries = retries,
            fuelLiters = fuelLiters,
            segment = segment,
            locale = locale,
            externalRef = externalRef,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a availability milestone in status planned. */
fun newAvailabilityMilestone(code: String, title: String): AvailabilityMilestone {
    return AvailabilityMilestone(
        code = code,
        title = title,
        pallets = 0,
        loadKg = 0.0,
        retries = 0,
        fuelLiters = 0.0,
        segment = "",
        locale = "",
        externalRef = "",
        currency = "",
        status = AvailabilityMilestoneStatus.PLANNED,
    )
}
