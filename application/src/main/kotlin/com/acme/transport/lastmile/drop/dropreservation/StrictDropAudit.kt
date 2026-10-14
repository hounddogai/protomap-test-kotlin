package com.acme.transport.lastmile.drop.dropreservation

import com.acme.transport.kit.text.normalize

/** Where a strict drop audit is in its life cycle. */
enum class StrictDropAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One strict drop audit, with what the planners need to act on it. */
data class StrictDropAudit(
    var code: String,
    var title: String,
    var archived: Boolean,
    var summary: String,
    var depot: String,
    var score: Double,
    var status: StrictDropAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDropAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDropAuditStatus): Boolean {
        when (status) {
            StrictDropAuditStatus.DRAFT -> {
                when (next) {
                    StrictDropAuditStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictDropAuditStatus.PENDING -> {
                when (next) {
                    StrictDropAuditStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictDropAuditStatus.ACTIVE -> return next == StrictDropAuditStatus.SUSPENDED
            StrictDropAuditStatus.SUSPENDED -> return next == StrictDropAuditStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: StrictDropAuditStatus) {
        if (!canMoveTo(next)) {
            error("strict drop audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict drop audit"} $code"

    fun measuredValue(): Double = score

    fun snapshot(): StrictDropAudit {
        return StrictDropAudit(
            code = code,
            title = title,
            archived = archived,
            summary = summary,
            depot = depot,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a strict drop audit in status draft. */
fun newStrictDropAudit(code: String, title: String): StrictDropAudit {
    return StrictDropAudit(
        code = code,
        title = title,
        archived = false,
        summary = "",
        depot = "",
        score = 0.0,
        status = StrictDropAuditStatus.DRAFT,
    )
}
