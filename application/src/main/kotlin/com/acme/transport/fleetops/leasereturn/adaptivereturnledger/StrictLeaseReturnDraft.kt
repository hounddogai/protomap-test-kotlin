package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict lease return draft is in its life cycle. */
enum class StrictLeaseReturnDraftStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One strict lease return draft, with what the planners need to act on it. */
data class StrictLeaseReturnDraft(
    var code: String,
    var label: String,
    var minimum: Double,
    var tags: MutableList<String>,
    var carrier: String,
    var cost: Double,
    var notes: String,
    var attempts: Int,
    var version: Int,
    var timezone: String,
    var status: StrictLeaseReturnDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictLeaseReturnDraftStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictLeaseReturnDraftStatus): Boolean {
        when (status) {
            StrictLeaseReturnDraftStatus.OPEN -> {
                when (next) {
                    StrictLeaseReturnDraftStatus.REOPENED -> return true
                }
                return false
            }
            StrictLeaseReturnDraftStatus.IN_REVIEW -> {
                when (next) {
                    StrictLeaseReturnDraftStatus.RESOLVED, StrictLeaseReturnDraftStatus.REOPENED -> return true
                }
                return false
            }
            StrictLeaseReturnDraftStatus.RESOLVED -> return next == StrictLeaseReturnDraftStatus.REOPENED || next == StrictLeaseReturnDraftStatus.ARCHIVED
            StrictLeaseReturnDraftStatus.REOPENED -> return next == StrictLeaseReturnDraftStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictLeaseReturnDraftStatus) {
        if (!canMoveTo(next)) {
            error("strict lease return draft $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code: $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedVolume(): Double = round(cost * version.toDouble() * 100) / 100

    fun snapshot(): StrictLeaseReturnDraft {
        return StrictLeaseReturnDraft(
            code = code,
            label = label,
            minimum = minimum,
            tags = tags,
            carrier = carrier,
            cost = cost,
            notes = notes,
            attempts = attempts,
            version = version,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict lease return draft in status open. */
fun newStrictLeaseReturnDraft(code: String, label: String): StrictLeaseReturnDraft {
    return StrictLeaseReturnDraft(
        code = code,
        label = label,
        minimum = 0.0,
        tags = mutableListOf<String>(),
        carrier = "",
        cost = 0.0,
        notes = "",
        attempts = 0,
        version = 0,
        timezone = "",
        status = StrictLeaseReturnDraftStatus.OPEN,
    )
}
