package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.kit.text.normalize

/** Where a scheduled dock door draft is in its life cycle. */
enum class ScheduledDockDoorDraftStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One scheduled dock door draft, with what the planners need to act on it. */
data class ScheduledDockDoorDraft(
    var code: String,
    var title: String,
    var odometer: Int,
    var contactEmail: String,
    var timezone: String,
    var status: ScheduledDockDoorDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDockDoorDraftStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDockDoorDraftStatus): Boolean {
        when (status) {
            ScheduledDockDoorDraftStatus.OPEN -> return next == ScheduledDockDoorDraftStatus.IN_REVIEW
            ScheduledDockDoorDraftStatus.IN_REVIEW -> return next == ScheduledDockDoorDraftStatus.RESOLVED
            ScheduledDockDoorDraftStatus.RESOLVED -> {
                when (next) {
                    ScheduledDockDoorDraftStatus.REOPENED, ScheduledDockDoorDraftStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledDockDoorDraftStatus.REOPENED -> return next == ScheduledDockDoorDraftStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledDockDoorDraftStatus) {
        if (!canMoveTo(next)) {
            error("scheduled dock door draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): ScheduledDockDoorDraft {
        return ScheduledDockDoorDraft(
            code = code,
            title = title,
            odometer = odometer,
            contactEmail = contactEmail,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled dock door draft in status open. */
fun newScheduledDockDoorDraft(code: String, title: String): ScheduledDockDoorDraft {
    return ScheduledDockDoorDraft(
        code = code,
        title = title,
        odometer = 0,
        contactEmail = "",
        timezone = "",
        status = ScheduledDockDoorDraftStatus.OPEN,
    )
}
