package com.acme.transport.network.cutoff.draft

import com.acme.transport.kit.text.normalize

// The cutoff draft records of network's cutoff work, and the rules, stores, and calculations around them.

/** Where a cutoff draft is in its life cycle. */
enum class CutoffDraftStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A cutoff draft as the cutoff team keeps it. */
data class CutoffDraft(
    var code: String,
    var label: String,
    var segment: String,
    var approved: Boolean,
    var routeCode: String,
    var dock: String,
    var unit: String,
    var reviewer: String,
    var score: Double,
    var status: CutoffDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffDraftStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffDraftStatus): Boolean {
        when (status) {
            CutoffDraftStatus.REQUESTED -> return next == CutoffDraftStatus.EXPIRED
            CutoffDraftStatus.GRANTED -> return next == CutoffDraftStatus.REVOKED || next == CutoffDraftStatus.EXPIRED
            CutoffDraftStatus.REVOKED -> {
                when (next) {
                    CutoffDraftStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CutoffDraftStatus) {
        if (!canMoveTo(next)) {
            error("cutoff draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = score

    fun snapshot(): CutoffDraft {
        return CutoffDraft(
            code = code,
            label = label,
            segment = segment,
            approved = approved,
            routeCode = routeCode,
            dock = dock,
            unit = unit,
            reviewer = reviewer,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a cutoff draft in status requested. */
fun newCutoffDraft(code: String, label: String): CutoffDraft {
    return CutoffDraft(
        code = code,
        label = label,
        segment = "",
        approved = false,
        routeCode = "",
        dock = "",
        unit = "",
        reviewer = "",
        score = 0.0,
        status = CutoffDraftStatus.REQUESTED,
    )
}
