package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The availability profile records of crewing's availability work, and the rules, stores, and calculations
// around them.

/** Where a availability profile is in its life cycle. */
enum class AvailabilityProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A availability profile as the availability team keeps it. */
data class AvailabilityProfile(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var ownerId: String,
    var capacity: Int,
    var status: AvailabilityProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AvailabilityProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AvailabilityProfileStatus): Boolean {
        when (status) {
            AvailabilityProfileStatus.OPEN -> return next == AvailabilityProfileStatus.REOPENED || next == AvailabilityProfileStatus.ARCHIVED
            AvailabilityProfileStatus.IN_REVIEW -> {
                when (next) {
                    AvailabilityProfileStatus.RESOLVED, AvailabilityProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            AvailabilityProfileStatus.RESOLVED -> {
                when (next) {
                    AvailabilityProfileStatus.REOPENED, AvailabilityProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            AvailabilityProfileStatus.REOPENED -> {
                when (next) {
                    AvailabilityProfileStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AvailabilityProfileStatus) {
        if (!canMoveTo(next)) {
            error("availability profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netExposure(): Double = round((capacity.toDouble() - fuelLiters) * 100) / 100

    fun snapshot(): AvailabilityProfile {
        return AvailabilityProfile(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            ownerId = ownerId,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a availability profile in status open. */
fun newAvailabilityProfile(code: String, title: String): AvailabilityProfile {
    return AvailabilityProfile(
        code = code,
        title = title,
        fuelLiters = 0.0,
        ownerId = "",
        capacity = 0,
        status = AvailabilityProfileStatus.OPEN,
    )
}
