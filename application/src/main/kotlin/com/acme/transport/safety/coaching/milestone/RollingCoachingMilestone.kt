package com.acme.transport.safety.coaching.milestone

import com.acme.transport.kit.text.normalize

// The rolling coaching milestone records of safety's coaching work, and the rules, stores, and calculations
// around them.

/** Where a rolling coaching milestone is in its life cycle. */
enum class RollingCoachingMilestoneStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One rolling coaching milestone, with what the planners need to act on it. */
data class RollingCoachingMilestone(
    var code: String,
    var title: String,
    var currency: String,
    var region: String,
    var locale: String,
    var ownerId: String,
    var dock: String,
    var capacity: Int,
    var status: RollingCoachingMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCoachingMilestoneStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCoachingMilestoneStatus): Boolean {
        when (status) {
            RollingCoachingMilestoneStatus.PROPOSED -> {
                when (next) {
                    RollingCoachingMilestoneStatus.APPROVED -> return true
                }
                return false
            }
            RollingCoachingMilestoneStatus.APPROVED -> return next == RollingCoachingMilestoneStatus.REJECTED
            RollingCoachingMilestoneStatus.REJECTED -> {
                when (next) {
                    RollingCoachingMilestoneStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingCoachingMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("rolling coaching milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling coaching milestone"} $code"

    fun measuredValue(): Double = capacity.toDouble()

    fun snapshot(): RollingCoachingMilestone {
        return RollingCoachingMilestone(
            code = code,
            title = title,
            currency = currency,
            region = region,
            locale = locale,
            ownerId = ownerId,
            dock = dock,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling coaching milestone in status proposed. */
fun newRollingCoachingMilestone(code: String, title: String): RollingCoachingMilestone {
    return RollingCoachingMilestone(
        code = code,
        title = title,
        currency = "",
        region = "",
        locale = "",
        ownerId = "",
        dock = "",
        capacity = 0,
        status = RollingCoachingMilestoneStatus.PROPOSED,
    )
}
