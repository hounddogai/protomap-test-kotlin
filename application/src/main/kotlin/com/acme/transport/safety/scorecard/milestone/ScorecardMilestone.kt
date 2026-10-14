package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scorecard milestone records of safety's scorecard work, and the rules, stores, and calculations around
// them.

/** Where a scorecard milestone is in its life cycle. */
enum class ScorecardMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scorecard milestone, with what the planners need to act on it. */
data class ScorecardMilestone(
    var code: String,
    var title: String,
    var odometer: Int,
    var stopCount: Int,
    var retries: Int,
    var label: String,
    var score: Double,
    var approved: Boolean,
    var status: ScorecardMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardMilestoneStatus): Boolean {
        when (status) {
            ScorecardMilestoneStatus.CREATED -> return next == ScorecardMilestoneStatus.VALIDATED || next == ScorecardMilestoneStatus.PUBLISHED
            ScorecardMilestoneStatus.VALIDATED -> {
                when (next) {
                    ScorecardMilestoneStatus.PUBLISHED, ScorecardMilestoneStatus.RETIRED -> return true
                }
                return false
            }
            ScorecardMilestoneStatus.PUBLISHED -> return next == ScorecardMilestoneStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScorecardMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("scorecard milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun projectedLoad(): Double = round(retries.toDouble() * score * 100) / 100

    fun snapshot(): ScorecardMilestone {
        return ScorecardMilestone(
            code = code,
            title = title,
            odometer = odometer,
            stopCount = stopCount,
            retries = retries,
            label = label,
            score = score,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scorecard milestone in status created. */
fun newScorecardMilestone(code: String, title: String): ScorecardMilestone {
    return ScorecardMilestone(
        code = code,
        title = title,
        odometer = 0,
        stopCount = 0,
        retries = 0,
        label = "",
        score = 0.0,
        approved = false,
        status = ScorecardMilestoneStatus.CREATED,
    )
}
