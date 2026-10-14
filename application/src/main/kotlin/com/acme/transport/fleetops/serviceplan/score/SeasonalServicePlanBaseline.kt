package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal service plan baseline is in its life cycle. */
enum class SeasonalServicePlanBaselineStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A seasonal service plan baseline as the service plan team keeps it. */
data class SeasonalServicePlanBaseline(
    var code: String,
    var label: String,
    var weight: Double,
    var ownerId: String,
    var distanceKm: Double,
    var status: SeasonalServicePlanBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalServicePlanBaselineStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalServicePlanBaselineStatus): Boolean {
        when (status) {
            SeasonalServicePlanBaselineStatus.DRAFT -> {
                when (next) {
                    SeasonalServicePlanBaselineStatus.ACTIVE, SeasonalServicePlanBaselineStatus.SUSPENDED -> return true
                }
                return false
            }
            SeasonalServicePlanBaselineStatus.PENDING -> {
                when (next) {
                    SeasonalServicePlanBaselineStatus.ACTIVE, SeasonalServicePlanBaselineStatus.SUSPENDED -> return true
                }
                return false
            }
            SeasonalServicePlanBaselineStatus.ACTIVE -> {
                when (next) {
                    SeasonalServicePlanBaselineStatus.SUSPENDED, SeasonalServicePlanBaselineStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalServicePlanBaselineStatus.SUSPENDED -> return next == SeasonalServicePlanBaselineStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SeasonalServicePlanBaselineStatus) {
        if (!canMoveTo(next)) {
            error("seasonal service plan baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal service plan baseline"} $code"

    fun weightedTotal(): Double = round(distanceKm * weight * 100) / 100

    fun snapshot(): SeasonalServicePlanBaseline {
        return SeasonalServicePlanBaseline(
            code = code,
            label = label,
            weight = weight,
            ownerId = ownerId,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal service plan baseline in status draft. */
fun newSeasonalServicePlanBaseline(code: String, label: String): SeasonalServicePlanBaseline {
    return SeasonalServicePlanBaseline(
        code = code,
        label = label,
        weight = 0.0,
        ownerId = "",
        distanceKm = 0.0,
        status = SeasonalServicePlanBaselineStatus.DRAFT,
    )
}
