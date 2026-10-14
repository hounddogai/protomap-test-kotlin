package com.acme.transport.freightpay.audit.auditclaim

import com.acme.transport.kit.text.normalize

/** Where a capped audit target is in its life cycle. */
enum class CappedAuditTargetStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A capped audit target as the audit team keeps it. */
data class CappedAuditTarget(
    var code: String,
    var name: String,
    var checksum: String,
    var currency: String,
    var expiresAt: Int,
    var title: String,
    var status: CappedAuditTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAuditTargetStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAuditTargetStatus): Boolean {
        when (status) {
            CappedAuditTargetStatus.PROPOSED -> {
                when (next) {
                    CappedAuditTargetStatus.WITHDRAWN -> return true
                }
                return false
            }
            CappedAuditTargetStatus.APPROVED -> return next == CappedAuditTargetStatus.REJECTED
            CappedAuditTargetStatus.REJECTED -> return next == CappedAuditTargetStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CappedAuditTargetStatus) {
        if (!canMoveTo(next)) {
            error("capped audit target $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"capped audit target"} $code"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): CappedAuditTarget {
        return CappedAuditTarget(
            code = code,
            name = name,
            checksum = checksum,
            currency = currency,
            expiresAt = expiresAt,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a capped audit target in status proposed. */
fun newCappedAuditTarget(code: String, name: String): CappedAuditTarget {
    return CappedAuditTarget(
        code = code,
        name = name,
        checksum = "",
        currency = "",
        expiresAt = 0,
        title = "",
        status = CappedAuditTargetStatus.PROPOSED,
    )
}
