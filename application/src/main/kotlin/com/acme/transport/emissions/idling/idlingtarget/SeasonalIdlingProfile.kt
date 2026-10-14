package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal idling profile is in its life cycle. */
enum class SeasonalIdlingProfileStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A seasonal idling profile. Its code is unique within a tenant. */
data class SeasonalIdlingProfile(
    var code: String,
    var label: String,
    var cost: Double,
    var budget: Double,
    var reviewer: String,
    var approved: Boolean,
    var status: SeasonalIdlingProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalIdlingProfileStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalIdlingProfileStatus): Boolean {
        when (status) {
            SeasonalIdlingProfileStatus.PLANNED -> {
                when (next) {
                    SeasonalIdlingProfileStatus.SCHEDULED, SeasonalIdlingProfileStatus.COMPLETED -> return true
                }
                return false
            }
            SeasonalIdlingProfileStatus.SCHEDULED -> return next == SeasonalIdlingProfileStatus.IN_PROGRESS || next == SeasonalIdlingProfileStatus.CANCELLED
            SeasonalIdlingProfileStatus.IN_PROGRESS -> {
                when (next) {
                    SeasonalIdlingProfileStatus.COMPLETED -> return true
                }
                return false
            }
            SeasonalIdlingProfileStatus.COMPLETED -> return next == SeasonalIdlingProfileStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalIdlingProfileStatus) {
        if (!canMoveTo(next)) {
            error("seasonal idling profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun effectiveLoad(): Double = round(cost * budget * 100) / 100

    fun snapshot(): SeasonalIdlingProfile {
        return SeasonalIdlingProfile(
            code = code,
            label = label,
            cost = cost,
            budget = budget,
            reviewer = reviewer,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal idling profile in status planned. */
fun newSeasonalIdlingProfile(code: String, label: String): SeasonalIdlingProfile {
    return SeasonalIdlingProfile(
        code = code,
        label = label,
        cost = 0.0,
        budget = 0.0,
        reviewer = "",
        approved = false,
        status = SeasonalIdlingProfileStatus.PLANNED,
    )
}
