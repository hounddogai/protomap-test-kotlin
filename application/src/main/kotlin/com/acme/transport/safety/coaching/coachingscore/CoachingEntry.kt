package com.acme.transport.safety.coaching.coachingscore

import com.acme.transport.kit.text.normalize

/** Where a coaching entry is in its life cycle. */
enum class CoachingEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A coaching entry as the coaching team keeps it. */
data class CoachingEntry(
    var code: String,
    var title: String,
    var summary: String,
    var timezone: String,
    var amount: Double,
    var channel: String,
    var status: CoachingEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CoachingEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CoachingEntryStatus): Boolean {
        when (status) {
            CoachingEntryStatus.OPEN -> return next == CoachingEntryStatus.REOPENED
            CoachingEntryStatus.IN_REVIEW -> return next == CoachingEntryStatus.RESOLVED || next == CoachingEntryStatus.REOPENED
            CoachingEntryStatus.RESOLVED -> return next == CoachingEntryStatus.REOPENED || next == CoachingEntryStatus.ARCHIVED
            CoachingEntryStatus.REOPENED -> return next == CoachingEntryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CoachingEntryStatus) {
        if (!canMoveTo(next)) {
            error("coaching entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = amount

    fun snapshot(): CoachingEntry {
        return CoachingEntry(
            code = code,
            title = title,
            summary = summary,
            timezone = timezone,
            amount = amount,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a coaching entry in status open. */
fun newCoachingEntry(code: String, title: String): CoachingEntry {
    return CoachingEntry(
        code = code,
        title = title,
        summary = "",
        timezone = "",
        amount = 0.0,
        channel = "",
        status = CoachingEntryStatus.OPEN,
    )
}
