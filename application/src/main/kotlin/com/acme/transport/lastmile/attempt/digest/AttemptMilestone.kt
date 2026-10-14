package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.kit.text.normalize

/** Where a attempt milestone is in its life cycle. */
enum class AttemptMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a attempt milestone and its current status. */
data class AttemptMilestone(
    var code: String,
    var title: String,
    var endsAt: Int,
    var description: String,
    var dock: String,
    var locale: String,
    var status: AttemptMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptMilestoneStatus): Boolean {
        when (status) {
            AttemptMilestoneStatus.CREATED -> return next == AttemptMilestoneStatus.VALIDATED || next == AttemptMilestoneStatus.RETIRED
            AttemptMilestoneStatus.VALIDATED -> return next == AttemptMilestoneStatus.PUBLISHED || next == AttemptMilestoneStatus.RETIRED
            AttemptMilestoneStatus.PUBLISHED -> return next == AttemptMilestoneStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AttemptMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("attempt milestone $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"attempt milestone"} $code"

    fun measuredValue(): Double = endsAt.toDouble()

    fun snapshot(): AttemptMilestone {
        return AttemptMilestone(
            code = code,
            title = title,
            endsAt = endsAt,
            description = description,
            dock = dock,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a attempt milestone in status created. */
fun newAttemptMilestone(code: String, title: String): AttemptMilestone {
    return AttemptMilestone(
        code = code,
        title = title,
        endsAt = 0,
        description = "",
        dock = "",
        locale = "",
        status = AttemptMilestoneStatus.CREATED,
    )
}
