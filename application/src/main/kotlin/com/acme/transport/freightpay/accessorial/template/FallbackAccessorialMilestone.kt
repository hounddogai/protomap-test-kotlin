package com.acme.transport.freightpay.accessorial.template

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback accessorial milestone is in its life cycle. */
enum class FallbackAccessorialMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a fallback accessorial milestone and its current status. */
data class FallbackAccessorialMilestone(
    var code: String,
    var title: String,
    var weight: Double,
    var graceDays: Int,
    var distanceKm: Double,
    var timezone: String,
    var status: FallbackAccessorialMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAccessorialMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAccessorialMilestoneStatus): Boolean {
        when (status) {
            FallbackAccessorialMilestoneStatus.CREATED -> {
                when (next) {
                    FallbackAccessorialMilestoneStatus.PUBLISHED -> return true
                }
                return false
            }
            FallbackAccessorialMilestoneStatus.VALIDATED -> {
                when (next) {
                    FallbackAccessorialMilestoneStatus.PUBLISHED, FallbackAccessorialMilestoneStatus.RETIRED -> return true
                }
                return false
            }
            FallbackAccessorialMilestoneStatus.PUBLISHED -> return next == FallbackAccessorialMilestoneStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackAccessorialMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("fallback accessorial milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedLoad(): Double = round((distanceKm + weight) * 100) / 100

    fun snapshot(): FallbackAccessorialMilestone {
        return FallbackAccessorialMilestone(
            code = code,
            title = title,
            weight = weight,
            graceDays = graceDays,
            distanceKm = distanceKm,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback accessorial milestone in status created. */
fun newFallbackAccessorialMilestone(code: String, title: String): FallbackAccessorialMilestone {
    return FallbackAccessorialMilestone(
        code = code,
        title = title,
        weight = 0.0,
        graceDays = 0,
        distanceKm = 0.0,
        timezone = "",
        status = FallbackAccessorialMilestoneStatus.CREATED,
    )
}
