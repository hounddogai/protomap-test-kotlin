package com.acme.transport.customs.hold.audit

import com.acme.transport.kit.text.normalize

// The pending hold audit records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a pending hold audit is in its life cycle. */
enum class PendingHoldAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a pending hold audit and its current status. */
data class PendingHoldAudit(
    var code: String,
    var title: String,
    var name: String,
    var ownerId: String,
    var version: Int,
    var status: PendingHoldAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingHoldAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingHoldAuditStatus): Boolean {
        when (status) {
            PendingHoldAuditStatus.OPEN -> return next == PendingHoldAuditStatus.IN_REVIEW || next == PendingHoldAuditStatus.RESOLVED
            PendingHoldAuditStatus.IN_REVIEW -> {
                when (next) {
                    PendingHoldAuditStatus.REOPENED -> return true
                }
                return false
            }
            PendingHoldAuditStatus.RESOLVED -> {
                when (next) {
                    PendingHoldAuditStatus.REOPENED, PendingHoldAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            PendingHoldAuditStatus.REOPENED -> return next == PendingHoldAuditStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PendingHoldAuditStatus) {
        if (!canMoveTo(next)) {
            error("pending hold audit $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): PendingHoldAudit {
        return PendingHoldAudit(
            code = code,
            title = title,
            name = name,
            ownerId = ownerId,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending hold audit in status open. */
fun newPendingHoldAudit(code: String, title: String): PendingHoldAudit {
    return PendingHoldAudit(
        code = code,
        title = title,
        name = "",
        ownerId = "",
        version = 0,
        status = PendingHoldAuditStatus.OPEN,
    )
}
