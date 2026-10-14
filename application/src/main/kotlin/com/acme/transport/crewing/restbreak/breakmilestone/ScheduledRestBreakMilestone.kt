package com.acme.transport.crewing.restbreak.breakmilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled rest break milestone records of crewing's rest break work, and the rules, stores, and
// calculations around them.

/** Where a scheduled rest break milestone is in its life cycle. */
enum class ScheduledRestBreakMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scheduled rest break milestone, with what the planners need to act on it. */
data class ScheduledRestBreakMilestone(
    var code: String,
    var name: String,
    var durationMinutes: Int,
    var region: String,
    var pinned: Boolean,
    var title: String,
    var contactEmail: String,
    var priority: Int,
    var status: ScheduledRestBreakMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRestBreakMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRestBreakMilestoneStatus): Boolean {
        when (status) {
            ScheduledRestBreakMilestoneStatus.CREATED -> {
                when (next) {
                    ScheduledRestBreakMilestoneStatus.PUBLISHED, ScheduledRestBreakMilestoneStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledRestBreakMilestoneStatus.VALIDATED -> return next == ScheduledRestBreakMilestoneStatus.RETIRED
            ScheduledRestBreakMilestoneStatus.PUBLISHED -> return next == ScheduledRestBreakMilestoneStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledRestBreakMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("scheduled rest break milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun effectiveTotal(): Double = round(durationMinutes.toDouble() * priority.toDouble() * 100) / 100

    fun snapshot(): ScheduledRestBreakMilestone {
        return ScheduledRestBreakMilestone(
            code = code,
            name = name,
            durationMinutes = durationMinutes,
            region = region,
            pinned = pinned,
            title = title,
            contactEmail = contactEmail,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled rest break milestone in status created. */
fun newScheduledRestBreakMilestone(code: String, name: String): ScheduledRestBreakMilestone {
    return ScheduledRestBreakMilestone(
        code = code,
        name = name,
        durationMinutes = 0,
        region = "",
        pinned = false,
        title = "",
        contactEmail = "",
        priority = 0,
        status = ScheduledRestBreakMilestoneStatus.CREATED,
    )
}
