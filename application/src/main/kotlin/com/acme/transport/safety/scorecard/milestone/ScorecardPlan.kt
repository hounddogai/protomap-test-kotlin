package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard plan is in its life cycle. */
enum class ScorecardPlanStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scorecard plan, with what the planners need to act on it. */
data class ScorecardPlan(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var maximum: Double,
    var summary: String,
    var quantity: Int,
    var approved: Boolean,
    var status: ScorecardPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardPlanStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardPlanStatus): Boolean {
        when (status) {
            ScorecardPlanStatus.CREATED -> {
                when (next) {
                    ScorecardPlanStatus.PUBLISHED, ScorecardPlanStatus.RETIRED -> return true
                }
                return false
            }
            ScorecardPlanStatus.VALIDATED -> {
                when (next) {
                    ScorecardPlanStatus.RETIRED -> return true
                }
                return false
            }
            ScorecardPlanStatus.PUBLISHED -> {
                when (next) {
                    ScorecardPlanStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardPlanStatus) {
        if (!canMoveTo(next)) {
            error("scorecard plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scorecard plan"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveValue(): Double = round((maximum - quantity.toDouble()) * 100) / 100

    fun snapshot(): ScorecardPlan {
        return ScorecardPlan(
            code = code,
            label = label,
            tags = tags,
            maximum = maximum,
            summary = summary,
            quantity = quantity,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scorecard plan in status created. */
fun newScorecardPlan(code: String, label: String): ScorecardPlan {
    return ScorecardPlan(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        maximum = 0.0,
        summary = "",
        quantity = 0,
        approved = false,
        status = ScorecardPlanStatus.CREATED,
    )
}
