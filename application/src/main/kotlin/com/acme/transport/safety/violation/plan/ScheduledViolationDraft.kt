package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.text.normalize

/** Where a scheduled violation draft is in its life cycle. */
enum class ScheduledViolationDraftStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled violation draft as the violation team keeps it. */
data class ScheduledViolationDraft(
    var code: String,
    var name: String,
    var active: Boolean,
    var timezone: String,
    var dock: String,
    var trailerId: String,
    var archived: Boolean,
    var notes: String,
    var contactEmail: String,
    var weight: Double,
    var driverId: String,
    var status: ScheduledViolationDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledViolationDraftStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledViolationDraftStatus): Boolean {
        when (status) {
            ScheduledViolationDraftStatus.OPEN -> {
                when (next) {
                    ScheduledViolationDraftStatus.RESOLVED, ScheduledViolationDraftStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledViolationDraftStatus.IN_REVIEW -> return next == ScheduledViolationDraftStatus.RESOLVED || next == ScheduledViolationDraftStatus.ARCHIVED
            ScheduledViolationDraftStatus.RESOLVED -> {
                when (next) {
                    ScheduledViolationDraftStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledViolationDraftStatus.REOPENED -> {
                when (next) {
                    ScheduledViolationDraftStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledViolationDraftStatus) {
        if (!canMoveTo(next)) {
            error("scheduled violation draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled violation draft"} $code"

    fun measuredValue(): Double = weight

    fun snapshot(): ScheduledViolationDraft {
        return ScheduledViolationDraft(
            code = code,
            name = name,
            active = active,
            timezone = timezone,
            dock = dock,
            trailerId = trailerId,
            archived = archived,
            notes = notes,
            contactEmail = contactEmail,
            weight = weight,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled violation draft in status open. */
fun newScheduledViolationDraft(code: String, name: String): ScheduledViolationDraft {
    return ScheduledViolationDraft(
        code = code,
        name = name,
        active = false,
        timezone = "",
        dock = "",
        trailerId = "",
        archived = false,
        notes = "",
        contactEmail = "",
        weight = 0.0,
        driverId = "",
        status = ScheduledViolationDraftStatus.OPEN,
    )
}
