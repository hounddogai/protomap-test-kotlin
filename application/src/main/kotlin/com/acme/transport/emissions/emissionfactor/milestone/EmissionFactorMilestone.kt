package com.acme.transport.emissions.emissionfactor.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The emission factor milestone records of emissions's emission factor work, and the rules, stores, and
// calculations around them.

/** Where a emission factor milestone is in its life cycle. */
enum class EmissionFactorMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A emission factor milestone as the emission factor team keeps it. */
data class EmissionFactorMilestone(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var distanceKm: Double,
    var summary: String,
    var limit: Int,
    var depot: String,
    var status: EmissionFactorMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorMilestoneStatus): Boolean {
        when (status) {
            EmissionFactorMilestoneStatus.DRAFT -> return next == EmissionFactorMilestoneStatus.ACTIVE || next == EmissionFactorMilestoneStatus.CLOSED
            EmissionFactorMilestoneStatus.PENDING -> return next == EmissionFactorMilestoneStatus.SUSPENDED
            EmissionFactorMilestoneStatus.ACTIVE -> {
                when (next) {
                    EmissionFactorMilestoneStatus.SUSPENDED, EmissionFactorMilestoneStatus.CLOSED -> return true
                }
                return false
            }
            EmissionFactorMilestoneStatus.SUSPENDED -> {
                when (next) {
                    EmissionFactorMilestoneStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: EmissionFactorMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("emission factor milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedLoad(): Double = round((distanceKm - limit.toDouble()) * 100) / 100

    fun snapshot(): EmissionFactorMilestone {
        return EmissionFactorMilestone(
            code = code,
            label = label,
            pinned = pinned,
            distanceKm = distanceKm,
            summary = summary,
            limit = limit,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a emission factor milestone in status draft. */
fun newEmissionFactorMilestone(code: String, label: String): EmissionFactorMilestone {
    return EmissionFactorMilestone(
        code = code,
        label = label,
        pinned = false,
        distanceKm = 0.0,
        summary = "",
        limit = 0,
        depot = "",
        status = EmissionFactorMilestoneStatus.DRAFT,
    )
}
