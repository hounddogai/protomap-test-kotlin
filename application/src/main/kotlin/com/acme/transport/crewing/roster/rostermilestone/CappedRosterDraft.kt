package com.acme.transport.crewing.roster.rostermilestone

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped roster draft is in its life cycle. */
enum class CappedRosterDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A capped roster draft as the roster team keeps it. */
data class CappedRosterDraft(
    var code: String,
    var label: String,
    var active: Boolean,
    var ratio: Double,
    var score: Double,
    var status: CappedRosterDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedRosterDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedRosterDraftStatus): Boolean {
        when (status) {
            CappedRosterDraftStatus.IDLE -> return next == CappedRosterDraftStatus.READY || next == CappedRosterDraftStatus.DRAINING
            CappedRosterDraftStatus.WARMING -> {
                when (next) {
                    CappedRosterDraftStatus.READY, CappedRosterDraftStatus.STOPPED -> return true
                }
                return false
            }
            CappedRosterDraftStatus.READY -> return next == CappedRosterDraftStatus.DRAINING || next == CappedRosterDraftStatus.STOPPED
            CappedRosterDraftStatus.DRAINING -> {
                when (next) {
                    CappedRosterDraftStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedRosterDraftStatus) {
        if (!canMoveTo(next)) {
            error("capped roster draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"capped roster draft"} $code"

    fun adjustedExposure(): Double = round((ratio + score) * 100) / 100

    fun snapshot(): CappedRosterDraft {
        return CappedRosterDraft(
            code = code,
            label = label,
            active = active,
            ratio = ratio,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped roster draft in status idle. */
fun newCappedRosterDraft(code: String, label: String): CappedRosterDraft {
    return CappedRosterDraft(
        code = code,
        label = label,
        active = false,
        ratio = 0.0,
        score = 0.0,
        status = CappedRosterDraftStatus.IDLE,
    )
}
