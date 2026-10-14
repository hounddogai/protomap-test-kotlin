package com.acme.transport.lastmile.attempt.milestone

import com.acme.transport.kit.text.normalize

// The fallback attempt milestone records of lastmile's attempt work, and the rules, stores, and calculations
// around them.

/** Where a fallback attempt milestone is in its life cycle. */
enum class FallbackAttemptMilestoneStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One fallback attempt milestone, with what the planners need to act on it. */
data class FallbackAttemptMilestone(
    var code: String,
    var label: String,
    var description: String,
    var archived: Boolean,
    var shiftCode: String,
    var graceDays: Int,
    var status: FallbackAttemptMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAttemptMilestoneStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAttemptMilestoneStatus): Boolean {
        when (status) {
            FallbackAttemptMilestoneStatus.DRAFT -> return next == FallbackAttemptMilestoneStatus.SUSPENDED
            FallbackAttemptMilestoneStatus.PENDING -> return next == FallbackAttemptMilestoneStatus.ACTIVE || next == FallbackAttemptMilestoneStatus.SUSPENDED
            FallbackAttemptMilestoneStatus.ACTIVE -> return next == FallbackAttemptMilestoneStatus.SUSPENDED || next == FallbackAttemptMilestoneStatus.CLOSED
            FallbackAttemptMilestoneStatus.SUSPENDED -> {
                when (next) {
                    FallbackAttemptMilestoneStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackAttemptMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("fallback attempt milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = graceDays.toDouble()

    fun snapshot(): FallbackAttemptMilestone {
        return FallbackAttemptMilestone(
            code = code,
            label = label,
            description = description,
            archived = archived,
            shiftCode = shiftCode,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback attempt milestone in status draft. */
fun newFallbackAttemptMilestone(code: String, label: String): FallbackAttemptMilestone {
    return FallbackAttemptMilestone(
        code = code,
        label = label,
        description = "",
        archived = false,
        shiftCode = "",
        graceDays = 0,
        status = FallbackAttemptMilestoneStatus.DRAFT,
    )
}
