package com.acme.transport.lastmile.attempt.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient attempt audit is in its life cycle. */
enum class LenientAttemptAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One lenient attempt audit, with what the planners need to act on it. */
data class LenientAttemptAudit(
    var code: String,
    var name: String,
    var retries: Int,
    var visible: Boolean,
    var capacity: Int,
    var title: String,
    var status: LenientAttemptAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientAttemptAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientAttemptAuditStatus): Boolean {
        when (status) {
            LenientAttemptAuditStatus.OPEN -> {
                when (next) {
                    LenientAttemptAuditStatus.IN_REVIEW, LenientAttemptAuditStatus.RESOLVED -> return true
                }
                return false
            }
            LenientAttemptAuditStatus.IN_REVIEW -> return next == LenientAttemptAuditStatus.RESOLVED
            LenientAttemptAuditStatus.RESOLVED -> {
                when (next) {
                    LenientAttemptAuditStatus.REOPENED, LenientAttemptAuditStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientAttemptAuditStatus.REOPENED -> {
                when (next) {
                    LenientAttemptAuditStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientAttemptAuditStatus) {
        if (!canMoveTo(next)) {
            error("lenient attempt audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedValue(): Double = round((capacity.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): LenientAttemptAudit {
        return LenientAttemptAudit(
            code = code,
            name = name,
            retries = retries,
            visible = visible,
            capacity = capacity,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient attempt audit in status open. */
fun newLenientAttemptAudit(code: String, name: String): LenientAttemptAudit {
    return LenientAttemptAudit(
        code = code,
        name = name,
        retries = 0,
        visible = false,
        capacity = 0,
        title = "",
        status = LenientAttemptAuditStatus.OPEN,
    )
}
