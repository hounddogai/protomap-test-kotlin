package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.kit.text.normalize

/** Where a rest break milestone is in its life cycle. */
enum class RestBreakMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rest break milestone. Its code is unique within a tenant. */
data class RestBreakMilestone(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var channel: String,
    var active: Boolean,
    var sequence: Int,
    var lane: String,
    var status: RestBreakMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakMilestoneStatus): Boolean {
        when (status) {
            RestBreakMilestoneStatus.PLANNED -> return next == RestBreakMilestoneStatus.CANCELLED
            RestBreakMilestoneStatus.SCHEDULED -> {
                when (next) {
                    RestBreakMilestoneStatus.COMPLETED -> return true
                }
                return false
            }
            RestBreakMilestoneStatus.IN_PROGRESS -> {
                when (next) {
                    RestBreakMilestoneStatus.COMPLETED, RestBreakMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            RestBreakMilestoneStatus.COMPLETED -> {
                when (next) {
                    RestBreakMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("rest break milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = sequence.toDouble()

    fun snapshot(): RestBreakMilestone {
        return RestBreakMilestone(
            code = code,
            title = title,
            pinned = pinned,
            channel = channel,
            active = active,
            sequence = sequence,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rest break milestone in status planned. */
fun newRestBreakMilestone(code: String, title: String): RestBreakMilestone {
    return RestBreakMilestone(
        code = code,
        title = title,
        pinned = false,
        channel = "",
        active = false,
        sequence = 0,
        lane = "",
        status = RestBreakMilestoneStatus.PLANNED,
    )
}
