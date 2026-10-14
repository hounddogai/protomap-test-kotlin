package com.acme.transport.lastmile.locker.lockerpreference

import com.acme.transport.kit.text.normalize

/** Where a pending locker audit is in its life cycle. */
enum class PendingLockerAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A pending locker audit as the locker team keeps it. */
data class PendingLockerAudit(
    var code: String,
    var title: String,
    var stopCount: Int,
    var tags: MutableList<String>,
    var phone: String,
    var category: String,
    var status: PendingLockerAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingLockerAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingLockerAuditStatus): Boolean {
        when (status) {
            PendingLockerAuditStatus.OPEN -> return next == PendingLockerAuditStatus.ARCHIVED
            PendingLockerAuditStatus.IN_REVIEW -> {
                when (next) {
                    PendingLockerAuditStatus.RESOLVED, PendingLockerAuditStatus.REOPENED -> return true
                }
                return false
            }
            PendingLockerAuditStatus.RESOLVED -> return next == PendingLockerAuditStatus.REOPENED
            PendingLockerAuditStatus.REOPENED -> return next == PendingLockerAuditStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PendingLockerAuditStatus) {
        if (!canMoveTo(next)) {
            error("pending locker audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending locker audit"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = stopCount.toDouble()

    fun snapshot(): PendingLockerAudit {
        return PendingLockerAudit(
            code = code,
            title = title,
            stopCount = stopCount,
            tags = tags,
            phone = phone,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending locker audit in status open. */
fun newPendingLockerAudit(code: String, title: String): PendingLockerAudit {
    return PendingLockerAudit(
        code = code,
        title = title,
        stopCount = 0,
        tags = mutableListOf<String>(),
        phone = "",
        category = "",
        status = PendingLockerAuditStatus.OPEN,
    )
}
