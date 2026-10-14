package com.acme.transport.safety.cameraevent.globaleventaudit

import com.acme.transport.kit.text.normalize

// The legacy camera event audit records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a legacy camera event audit is in its life cycle. */
enum class LegacyCameraEventAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One legacy camera event audit, with what the planners need to act on it. */
data class LegacyCameraEventAudit(
    var code: String,
    var title: String,
    var contactEmail: String,
    var ceiling: Double,
    var tags: MutableList<String>,
    var status: LegacyCameraEventAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCameraEventAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCameraEventAuditStatus): Boolean {
        when (status) {
            LegacyCameraEventAuditStatus.OPEN -> {
                when (next) {
                    LegacyCameraEventAuditStatus.RESOLVED -> return true
                }
                return false
            }
            LegacyCameraEventAuditStatus.IN_REVIEW -> return next == LegacyCameraEventAuditStatus.RESOLVED || next == LegacyCameraEventAuditStatus.ARCHIVED
            LegacyCameraEventAuditStatus.RESOLVED -> return next == LegacyCameraEventAuditStatus.ARCHIVED
            LegacyCameraEventAuditStatus.REOPENED -> return next == LegacyCameraEventAuditStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: LegacyCameraEventAuditStatus) {
        if (!canMoveTo(next)) {
            error("legacy camera event audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"legacy camera event audit"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = ceiling

    fun snapshot(): LegacyCameraEventAudit {
        return LegacyCameraEventAudit(
            code = code,
            title = title,
            contactEmail = contactEmail,
            ceiling = ceiling,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a legacy camera event audit in status open. */
fun newLegacyCameraEventAudit(code: String, title: String): LegacyCameraEventAudit {
    return LegacyCameraEventAudit(
        code = code,
        title = title,
        contactEmail = "",
        ceiling = 0.0,
        tags = mutableListOf<String>(),
        status = LegacyCameraEventAuditStatus.OPEN,
    )
}
