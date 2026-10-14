package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched scorecard milestone is in its life cycle. */
enum class BatchedScorecardMilestoneStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched scorecard milestone as the scorecard team keeps it. */
data class BatchedScorecardMilestone(
    var code: String,
    var title: String,
    var capacity: Int,
    var summary: String,
    var retries: Int,
    var driverId: String,
    var pinned: Boolean,
    var locale: String,
    var updatedAt: Int,
    var status: BatchedScorecardMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedScorecardMilestoneStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedScorecardMilestoneStatus): Boolean {
        when (status) {
            BatchedScorecardMilestoneStatus.OPEN -> return next == BatchedScorecardMilestoneStatus.REOPENED
            BatchedScorecardMilestoneStatus.IN_REVIEW -> {
                when (next) {
                    BatchedScorecardMilestoneStatus.REOPENED -> return true
                }
                return false
            }
            BatchedScorecardMilestoneStatus.RESOLVED -> return next == BatchedScorecardMilestoneStatus.REOPENED
            BatchedScorecardMilestoneStatus.REOPENED -> return next == BatchedScorecardMilestoneStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: BatchedScorecardMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("batched scorecard milestone $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun netLoad(): Double = round(updatedAt.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): BatchedScorecardMilestone {
        return BatchedScorecardMilestone(
            code = code,
            title = title,
            capacity = capacity,
            summary = summary,
            retries = retries,
            driverId = driverId,
            pinned = pinned,
            locale = locale,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a batched scorecard milestone in status open. */
fun newBatchedScorecardMilestone(code: String, title: String): BatchedScorecardMilestone {
    return BatchedScorecardMilestone(
        code = code,
        title = title,
        capacity = 0,
        summary = "",
        retries = 0,
        driverId = "",
        pinned = false,
        locale = "",
        updatedAt = 0,
        status = BatchedScorecardMilestoneStatus.OPEN,
    )
}
