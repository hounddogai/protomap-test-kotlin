package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.kit.text.normalize

/** Where a fallback locker draft is in its life cycle. */
enum class FallbackLockerDraftStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a fallback locker draft and its current status. */
data class FallbackLockerDraft(
    var code: String,
    var title: String,
    var region: String,
    var notes: String,
    var category: String,
    var active: Boolean,
    var pinned: Boolean,
    var attempts: Int,
    var description: String,
    var lane: String,
    var status: FallbackLockerDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackLockerDraftStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackLockerDraftStatus): Boolean {
        when (status) {
            FallbackLockerDraftStatus.OPEN -> return next == FallbackLockerDraftStatus.IN_REVIEW
            FallbackLockerDraftStatus.IN_REVIEW -> {
                when (next) {
                    FallbackLockerDraftStatus.RESOLVED -> return true
                }
                return false
            }
            FallbackLockerDraftStatus.RESOLVED -> return next == FallbackLockerDraftStatus.REOPENED || next == FallbackLockerDraftStatus.ARCHIVED
            FallbackLockerDraftStatus.REOPENED -> return next == FallbackLockerDraftStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackLockerDraftStatus) {
        if (!canMoveTo(next)) {
            error("fallback locker draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun measuredValue(): Double = attempts.toDouble()

    fun snapshot(): FallbackLockerDraft {
        return FallbackLockerDraft(
            code = code,
            title = title,
            region = region,
            notes = notes,
            category = category,
            active = active,
            pinned = pinned,
            attempts = attempts,
            description = description,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a fallback locker draft in status open. */
fun newFallbackLockerDraft(code: String, title: String): FallbackLockerDraft {
    return FallbackLockerDraft(
        code = code,
        title = title,
        region = "",
        notes = "",
        category = "",
        active = false,
        pinned = false,
        attempts = 0,
        description = "",
        lane = "",
        status = FallbackLockerDraftStatus.OPEN,
    )
}
