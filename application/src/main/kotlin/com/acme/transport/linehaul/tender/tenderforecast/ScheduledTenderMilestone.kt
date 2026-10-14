package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled tender milestone is in its life cycle. */
enum class ScheduledTenderMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One scheduled tender milestone, with what the planners need to act on it. */
data class ScheduledTenderMilestone(
    var code: String,
    var title: String,
    var version: Int,
    var active: Boolean,
    var archived: Boolean,
    var locale: String,
    var durationMinutes: Int,
    var endsAt: Int,
    var region: String,
    var status: ScheduledTenderMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTenderMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTenderMilestoneStatus): Boolean {
        when (status) {
            ScheduledTenderMilestoneStatus.IDLE -> return next == ScheduledTenderMilestoneStatus.WARMING || next == ScheduledTenderMilestoneStatus.READY
            ScheduledTenderMilestoneStatus.WARMING -> return next == ScheduledTenderMilestoneStatus.DRAINING
            ScheduledTenderMilestoneStatus.READY -> return next == ScheduledTenderMilestoneStatus.DRAINING || next == ScheduledTenderMilestoneStatus.STOPPED
            ScheduledTenderMilestoneStatus.DRAINING -> return next == ScheduledTenderMilestoneStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: ScheduledTenderMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("scheduled tender milestone $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code: $title"

    fun adjustedValue(): Double = round((endsAt.toDouble() - version.toDouble()) * 100) / 100

    fun snapshot(): ScheduledTenderMilestone {
        return ScheduledTenderMilestone(
            code = code,
            title = title,
            version = version,
            active = active,
            archived = archived,
            locale = locale,
            durationMinutes = durationMinutes,
            endsAt = endsAt,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a scheduled tender milestone in status idle. */
fun newScheduledTenderMilestone(code: String, title: String): ScheduledTenderMilestone {
    return ScheduledTenderMilestone(
        code = code,
        title = title,
        version = 0,
        active = false,
        archived = false,
        locale = "",
        durationMinutes = 0,
        endsAt = 0,
        region = "",
        status = ScheduledTenderMilestoneStatus.IDLE,
    )
}
