package com.acme.transport.network.hub.batchedhubmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive hub milestone records of network's hub work, and the rules, stores, and calculations around
// them.

/** Where a adaptive hub milestone is in its life cycle. */
enum class AdaptiveHubMilestoneStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One adaptive hub milestone, with what the planners need to act on it. */
data class AdaptiveHubMilestone(
    var code: String,
    var label: String,
    var limit: Int,
    var pallets: Int,
    var sequence: Int,
    var maximum: Double,
    var odometer: Int,
    var durationMinutes: Int,
    var depot: String,
    var status: AdaptiveHubMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveHubMilestoneStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveHubMilestoneStatus): Boolean {
        when (status) {
            AdaptiveHubMilestoneStatus.REQUESTED -> return next == AdaptiveHubMilestoneStatus.GRANTED || next == AdaptiveHubMilestoneStatus.REVOKED
            AdaptiveHubMilestoneStatus.GRANTED -> {
                when (next) {
                    AdaptiveHubMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
            AdaptiveHubMilestoneStatus.REVOKED -> {
                when (next) {
                    AdaptiveHubMilestoneStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveHubMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("adaptive hub milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"adaptive hub milestone"} $code"

    fun weightedValue(): Double = round((pallets.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveHubMilestone {
        return AdaptiveHubMilestone(
            code = code,
            label = label,
            limit = limit,
            pallets = pallets,
            sequence = sequence,
            maximum = maximum,
            odometer = odometer,
            durationMinutes = durationMinutes,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a adaptive hub milestone in status requested. */
fun newAdaptiveHubMilestone(code: String, label: String): AdaptiveHubMilestone {
    return AdaptiveHubMilestone(
        code = code,
        label = label,
        limit = 0,
        pallets = 0,
        sequence = 0,
        maximum = 0.0,
        odometer = 0,
        durationMinutes = 0,
        depot = "",
        status = AdaptiveHubMilestoneStatus.REQUESTED,
    )
}
