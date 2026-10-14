package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The duty period draft records of crewing's duty period work, and the rules, stores, and calculations around
// them.

/** Where a duty period draft is in its life cycle. */
enum class DutyPeriodDraftStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A duty period draft. Its code is unique within a tenant. */
data class DutyPeriodDraft(
    var code: String,
    var title: String,
    var externalRef: String,
    var label: String,
    var graceDays: Int,
    var driverId: String,
    var endsAt: Int,
    var status: DutyPeriodDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DutyPeriodDraftStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DutyPeriodDraftStatus): Boolean {
        when (status) {
            DutyPeriodDraftStatus.OPEN -> {
                when (next) {
                    DutyPeriodDraftStatus.IN_REVIEW, DutyPeriodDraftStatus.ARCHIVED -> return true
                }
                return false
            }
            DutyPeriodDraftStatus.IN_REVIEW -> {
                when (next) {
                    DutyPeriodDraftStatus.REOPENED -> return true
                }
                return false
            }
            DutyPeriodDraftStatus.RESOLVED -> return next == DutyPeriodDraftStatus.ARCHIVED
            DutyPeriodDraftStatus.REOPENED -> return next == DutyPeriodDraftStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: DutyPeriodDraftStatus) {
        if (!canMoveTo(next)) {
            error("duty period draft $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"duty period draft"} $code"

    fun adjustedExposure(): Double = round(graceDays.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): DutyPeriodDraft {
        return DutyPeriodDraft(
            code = code,
            title = title,
            externalRef = externalRef,
            label = label,
            graceDays = graceDays,
            driverId = driverId,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a duty period draft in status open. */
fun newDutyPeriodDraft(code: String, title: String): DutyPeriodDraft {
    return DutyPeriodDraft(
        code = code,
        title = title,
        externalRef = "",
        label = "",
        graceDays = 0,
        driverId = "",
        endsAt = 0,
        status = DutyPeriodDraftStatus.OPEN,
    )
}
