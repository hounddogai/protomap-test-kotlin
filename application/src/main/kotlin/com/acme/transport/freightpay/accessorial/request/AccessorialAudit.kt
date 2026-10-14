package com.acme.transport.freightpay.accessorial.request

import com.acme.transport.kit.text.normalize

/** Where a accessorial audit is in its life cycle. */
enum class AccessorialAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A accessorial audit. Its code is unique within a tenant. */
data class AccessorialAudit(
    var code: String,
    var label: String,
    var locale: String,
    var pallets: Int,
    var summary: String,
    var approved: Boolean,
    var status: AccessorialAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AccessorialAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AccessorialAuditStatus): Boolean {
        when (status) {
            AccessorialAuditStatus.OPEN -> return next == AccessorialAuditStatus.REOPENED
            AccessorialAuditStatus.IN_REVIEW -> {
                when (next) {
                    AccessorialAuditStatus.RESOLVED -> return true
                }
                return false
            }
            AccessorialAuditStatus.RESOLVED -> return next == AccessorialAuditStatus.REOPENED || next == AccessorialAuditStatus.ARCHIVED
            AccessorialAuditStatus.REOPENED -> return next == AccessorialAuditStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AccessorialAuditStatus) {
        if (!canMoveTo(next)) {
            error("accessorial audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): AccessorialAudit {
        return AccessorialAudit(
            code = code,
            label = label,
            locale = locale,
            pallets = pallets,
            summary = summary,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a accessorial audit in status open. */
fun newAccessorialAudit(code: String, label: String): AccessorialAudit {
    return AccessorialAudit(
        code = code,
        label = label,
        locale = "",
        pallets = 0,
        summary = "",
        approved = false,
        status = AccessorialAuditStatus.OPEN,
    )
}
