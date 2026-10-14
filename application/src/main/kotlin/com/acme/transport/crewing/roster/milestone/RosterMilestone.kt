package com.acme.transport.crewing.roster.milestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The roster milestone records of crewing's roster work, and the rules, stores, and calculations around them.

/** Where a roster milestone is in its life cycle. */
enum class RosterMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A roster milestone. Its code is unique within a tenant. */
data class RosterMilestone(
    var code: String,
    var title: String,
    var lane: String,
    var threshold: Double,
    var expiresAt: Int,
    var durationMinutes: Int,
    var rate: Double,
    var status: RosterMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RosterMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RosterMilestoneStatus): Boolean {
        when (status) {
            RosterMilestoneStatus.OPEN -> return next == RosterMilestoneStatus.IN_REVIEW || next == RosterMilestoneStatus.RESOLVED
            RosterMilestoneStatus.IN_REVIEW -> {
                when (next) {
                    RosterMilestoneStatus.REOPENED -> return true
                }
                return false
            }
            RosterMilestoneStatus.RESOLVED -> return next == RosterMilestoneStatus.REOPENED || next == RosterMilestoneStatus.ARCHIVED
            RosterMilestoneStatus.REOPENED -> {
                when (next) {
                    RosterMilestoneStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RosterMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("roster milestone $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"roster milestone"} $code"

    fun weightedExposure(): Double = round((expiresAt.toDouble() + rate) * 100) / 100

    fun snapshot(): RosterMilestone {
        return RosterMilestone(
            code = code,
            title = title,
            lane = lane,
            threshold = threshold,
            expiresAt = expiresAt,
            durationMinutes = durationMinutes,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a roster milestone in status open. */
fun newRosterMilestone(code: String, title: String): RosterMilestone {
    return RosterMilestone(
        code = code,
        title = title,
        lane = "",
        threshold = 0.0,
        expiresAt = 0,
        durationMinutes = 0,
        rate = 0.0,
        status = RosterMilestoneStatus.OPEN,
    )
}
