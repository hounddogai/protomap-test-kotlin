package com.acme.transport.freightpay.audit.digest

import com.acme.transport.kit.text.normalize

// The audit digest records of freightpay's audit work, and the rules, stores, and calculations around them.

/** Where a audit digest is in its life cycle. */
enum class AuditDigestStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One audit digest, with what the planners need to act on it. */
data class AuditDigest(
    var code: String,
    var label: String,
    var approved: Boolean,
    var ownerId: String,
    var threshold: Double,
    var status: AuditDigestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditDigestStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditDigestStatus): Boolean {
        when (status) {
            AuditDigestStatus.PLANNED -> return next == AuditDigestStatus.IN_PROGRESS
            AuditDigestStatus.SCHEDULED -> return next == AuditDigestStatus.CANCELLED
            AuditDigestStatus.IN_PROGRESS -> return next == AuditDigestStatus.COMPLETED || next == AuditDigestStatus.CANCELLED
            AuditDigestStatus.COMPLETED -> return next == AuditDigestStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AuditDigestStatus) {
        if (!canMoveTo(next)) {
            error("audit digest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = threshold

    fun snapshot(): AuditDigest {
        return AuditDigest(
            code = code,
            label = label,
            approved = approved,
            ownerId = ownerId,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a audit digest in status planned. */
fun newAuditDigest(code: String, label: String): AuditDigest {
    return AuditDigest(
        code = code,
        label = label,
        approved = false,
        ownerId = "",
        threshold = 0.0,
        status = AuditDigestStatus.PLANNED,
    )
}
