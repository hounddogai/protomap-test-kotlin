package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive scorecard group records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a adaptive scorecard group is in its life cycle. */
enum class AdaptiveScorecardGroupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One adaptive scorecard group, with what the planners need to act on it. */
data class AdaptiveScorecardGroup(
    var code: String,
    var title: String,
    var batchSize: Int,
    var distanceKm: Double,
    var ceiling: Double,
    var budget: Double,
    var currency: String,
    var status: AdaptiveScorecardGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveScorecardGroupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveScorecardGroupStatus): Boolean {
        when (status) {
            AdaptiveScorecardGroupStatus.OPEN -> return next == AdaptiveScorecardGroupStatus.RESOLVED
            AdaptiveScorecardGroupStatus.IN_REVIEW -> return next == AdaptiveScorecardGroupStatus.REOPENED
            AdaptiveScorecardGroupStatus.RESOLVED -> {
                when (next) {
                    AdaptiveScorecardGroupStatus.REOPENED, AdaptiveScorecardGroupStatus.ARCHIVED -> return true
                }
                return false
            }
            AdaptiveScorecardGroupStatus.REOPENED -> {
                when (next) {
                    AdaptiveScorecardGroupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveScorecardGroupStatus) {
        if (!canMoveTo(next)) {
            error("adaptive scorecard group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedVolume(): Double = round((batchSize.toDouble() + ceiling) * 100) / 100

    fun snapshot(): AdaptiveScorecardGroup {
        return AdaptiveScorecardGroup(
            code = code,
            title = title,
            batchSize = batchSize,
            distanceKm = distanceKm,
            ceiling = ceiling,
            budget = budget,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive scorecard group in status open. */
fun newAdaptiveScorecardGroup(code: String, title: String): AdaptiveScorecardGroup {
    return AdaptiveScorecardGroup(
        code = code,
        title = title,
        batchSize = 0,
        distanceKm = 0.0,
        ceiling = 0.0,
        budget = 0.0,
        currency = "",
        status = AdaptiveScorecardGroupStatus.OPEN,
    )
}
