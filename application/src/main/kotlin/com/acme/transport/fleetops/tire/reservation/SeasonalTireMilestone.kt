package com.acme.transport.fleetops.tire.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal tire milestone is in its life cycle. */
enum class SeasonalTireMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal tire milestone as the tire team keeps it. */
data class SeasonalTireMilestone(
    var code: String,
    var name: String,
    var graceDays: Int,
    var vehicleId: String,
    var source: String,
    var trailerId: String,
    var threshold: Double,
    var status: SeasonalTireMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTireMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTireMilestoneStatus): Boolean {
        when (status) {
            SeasonalTireMilestoneStatus.OPEN -> return next == SeasonalTireMilestoneStatus.IN_REVIEW
            SeasonalTireMilestoneStatus.IN_REVIEW -> return next == SeasonalTireMilestoneStatus.RESOLVED || next == SeasonalTireMilestoneStatus.ARCHIVED
            SeasonalTireMilestoneStatus.RESOLVED -> return next == SeasonalTireMilestoneStatus.ARCHIVED
            SeasonalTireMilestoneStatus.REOPENED -> {
                when (next) {
                    SeasonalTireMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTireMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("seasonal tire milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedLoad(): Double = round(graceDays.toDouble() * threshold * 100) / 100

    fun snapshot(): SeasonalTireMilestone {
        return SeasonalTireMilestone(
            code = code,
            name = name,
            graceDays = graceDays,
            vehicleId = vehicleId,
            source = source,
            trailerId = trailerId,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal tire milestone in status open. */
fun newSeasonalTireMilestone(code: String, name: String): SeasonalTireMilestone {
    return SeasonalTireMilestone(
        code = code,
        name = name,
        graceDays = 0,
        vehicleId = "",
        source = "",
        trailerId = "",
        threshold = 0.0,
        status = SeasonalTireMilestoneStatus.OPEN,
    )
}
