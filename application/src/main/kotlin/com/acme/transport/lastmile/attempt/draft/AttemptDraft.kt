package com.acme.transport.lastmile.attempt.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The attempt draft records of lastmile's attempt work, and the rules, stores, and calculations around them.

/** Where a attempt draft is in its life cycle. */
enum class AttemptDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A attempt draft as the attempt team keeps it. */
data class AttemptDraft(
    var code: String,
    var title: String,
    var pallets: Int,
    var odometer: Int,
    var ratio: Double,
    var dock: String,
    var depot: String,
    var status: AttemptDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptDraftStatus): Boolean {
        when (status) {
            AttemptDraftStatus.PROPOSED -> return next == AttemptDraftStatus.WITHDRAWN
            AttemptDraftStatus.APPROVED -> return next == AttemptDraftStatus.REJECTED || next == AttemptDraftStatus.WITHDRAWN
            AttemptDraftStatus.REJECTED -> return next == AttemptDraftStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AttemptDraftStatus) {
        if (!canMoveTo(next)) {
            error("attempt draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveExposure(): Double = round((odometer.toDouble() - pallets.toDouble()) * 100) / 100

    fun snapshot(): AttemptDraft {
        return AttemptDraft(
            code = code,
            title = title,
            pallets = pallets,
            odometer = odometer,
            ratio = ratio,
            dock = dock,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a attempt draft in status proposed. */
fun newAttemptDraft(code: String, title: String): AttemptDraft {
    return AttemptDraft(
        code = code,
        title = title,
        pallets = 0,
        odometer = 0,
        ratio = 0.0,
        dock = "",
        depot = "",
        status = AttemptDraftStatus.PROPOSED,
    )
}
