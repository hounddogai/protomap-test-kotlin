package com.acme.transport.safety.scorecard.scorecardbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling scorecard policy is in its life cycle. */
enum class RollingScorecardPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One rolling scorecard policy, with what the planners need to act on it. */
data class RollingScorecardPolicy(
    var code: String,
    var label: String,
    var batchSize: Int,
    var notes: String,
    var country: String,
    var createdAt: Int,
    var status: RollingScorecardPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingScorecardPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingScorecardPolicyStatus): Boolean {
        when (status) {
            RollingScorecardPolicyStatus.PLANNED -> {
                when (next) {
                    RollingScorecardPolicyStatus.SCHEDULED -> return true
                }
                return false
            }
            RollingScorecardPolicyStatus.SCHEDULED -> {
                when (next) {
                    RollingScorecardPolicyStatus.COMPLETED -> return true
                }
                return false
            }
            RollingScorecardPolicyStatus.IN_PROGRESS -> return next == RollingScorecardPolicyStatus.COMPLETED
            RollingScorecardPolicyStatus.COMPLETED -> return next == RollingScorecardPolicyStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RollingScorecardPolicyStatus) {
        if (!canMoveTo(next)) {
            error("rolling scorecard policy $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedValue(): Double = round((batchSize.toDouble() - createdAt.toDouble()) * 100) / 100

    fun snapshot(): RollingScorecardPolicy {
        return RollingScorecardPolicy(
            code = code,
            label = label,
            batchSize = batchSize,
            notes = notes,
            country = country,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a rolling scorecard policy in status planned. */
fun newRollingScorecardPolicy(code: String, label: String): RollingScorecardPolicy {
    return RollingScorecardPolicy(
        code = code,
        label = label,
        batchSize = 0,
        notes = "",
        country = "",
        createdAt = 0,
        status = RollingScorecardPolicyStatus.PLANNED,
    )
}
