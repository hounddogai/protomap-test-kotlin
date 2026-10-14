package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.text.normalize

// The rolling node draft records of network's node work, and the rules, stores, and calculations around them.

/** Where a rolling node draft is in its life cycle. */
enum class RollingNodeDraftStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rolling node draft. Its code is unique within a tenant. */
data class RollingNodeDraft(
    var code: String,
    var title: String,
    var trailerId: String,
    var routeCode: String,
    var notes: String,
    var amount: Double,
    var status: RollingNodeDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingNodeDraftStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingNodeDraftStatus): Boolean {
        when (status) {
            RollingNodeDraftStatus.PROPOSED -> return next == RollingNodeDraftStatus.APPROVED
            RollingNodeDraftStatus.APPROVED -> {
                when (next) {
                    RollingNodeDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingNodeDraftStatus.REJECTED -> {
                when (next) {
                    RollingNodeDraftStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingNodeDraftStatus) {
        if (!canMoveTo(next)) {
            error("rolling node draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = amount

    fun snapshot(): RollingNodeDraft {
        return RollingNodeDraft(
            code = code,
            title = title,
            trailerId = trailerId,
            routeCode = routeCode,
            notes = notes,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling node draft in status proposed. */
fun newRollingNodeDraft(code: String, title: String): RollingNodeDraft {
    return RollingNodeDraft(
        code = code,
        title = title,
        trailerId = "",
        routeCode = "",
        notes = "",
        amount = 0.0,
        status = RollingNodeDraftStatus.PROPOSED,
    )
}
