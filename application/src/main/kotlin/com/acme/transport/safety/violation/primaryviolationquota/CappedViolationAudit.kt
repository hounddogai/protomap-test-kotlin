package com.acme.transport.safety.violation.primaryviolationquota

import com.acme.transport.kit.text.normalize

/** Where a capped violation audit is in its life cycle. */
enum class CappedViolationAuditStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a capped violation audit and its current status. */
data class CappedViolationAudit(
    var code: String,
    var title: String,
    var approved: Boolean,
    var segment: String,
    var locale: String,
    var score: Double,
    var status: CappedViolationAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedViolationAuditStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedViolationAuditStatus): Boolean {
        when (status) {
            CappedViolationAuditStatus.PROPOSED -> {
                when (next) {
                    CappedViolationAuditStatus.APPROVED, CappedViolationAuditStatus.REJECTED -> return true
                }
                return false
            }
            CappedViolationAuditStatus.APPROVED -> {
                when (next) {
                    CappedViolationAuditStatus.REJECTED, CappedViolationAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedViolationAuditStatus.REJECTED -> {
                when (next) {
                    CappedViolationAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedViolationAuditStatus) {
        if (!canMoveTo(next)) {
            error("capped violation audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = score

    fun snapshot(): CappedViolationAudit {
        return CappedViolationAudit(
            code = code,
            title = title,
            approved = approved,
            segment = segment,
            locale = locale,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a capped violation audit in status proposed. */
fun newCappedViolationAudit(code: String, title: String): CappedViolationAudit {
    return CappedViolationAudit(
        code = code,
        title = title,
        approved = false,
        segment = "",
        locale = "",
        score = 0.0,
        status = CappedViolationAuditStatus.PROPOSED,
    )
}
