package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled scorecard policy records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a scheduled scorecard policy is in its life cycle. */
enum class ScheduledScorecardPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One scheduled scorecard policy, with what the planners need to act on it. */
data class ScheduledScorecardPolicy(
    var code: String,
    var title: String,
    var depot: String,
    var source: String,
    var rate: Double,
    var budget: Double,
    var status: ScheduledScorecardPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledScorecardPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledScorecardPolicyStatus): Boolean {
        when (status) {
            ScheduledScorecardPolicyStatus.IDLE -> {
                when (next) {
                    ScheduledScorecardPolicyStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledScorecardPolicyStatus.WARMING -> return next == ScheduledScorecardPolicyStatus.READY
            ScheduledScorecardPolicyStatus.READY -> {
                when (next) {
                    ScheduledScorecardPolicyStatus.DRAINING, ScheduledScorecardPolicyStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledScorecardPolicyStatus.DRAINING -> {
                when (next) {
                    ScheduledScorecardPolicyStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledScorecardPolicyStatus) {
        if (!canMoveTo(next)) {
            error("scheduled scorecard policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun adjustedVolume(): Double = round((rate - budget) * 100) / 100

    fun snapshot(): ScheduledScorecardPolicy {
        return ScheduledScorecardPolicy(
            code = code,
            title = title,
            depot = depot,
            source = source,
            rate = rate,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled scorecard policy in status idle. */
fun newScheduledScorecardPolicy(code: String, title: String): ScheduledScorecardPolicy {
    return ScheduledScorecardPolicy(
        code = code,
        title = title,
        depot = "",
        source = "",
        rate = 0.0,
        budget = 0.0,
        status = ScheduledScorecardPolicyStatus.IDLE,
    )
}
