package com.acme.transport.freightpay.audit.auditoverride

import com.acme.transport.kit.text.normalize

/** Where a global audit bundle is in its life cycle. */
enum class GlobalAuditBundleStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A global audit bundle. Its code is unique within a tenant. */
data class GlobalAuditBundle(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var notes: String,
    var windowDays: Int,
    var status: GlobalAuditBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAuditBundleStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAuditBundleStatus): Boolean {
        when (status) {
            GlobalAuditBundleStatus.REQUESTED -> return next == GlobalAuditBundleStatus.GRANTED || next == GlobalAuditBundleStatus.EXPIRED
            GlobalAuditBundleStatus.GRANTED -> return next == GlobalAuditBundleStatus.REVOKED || next == GlobalAuditBundleStatus.EXPIRED
            GlobalAuditBundleStatus.REVOKED -> return next == GlobalAuditBundleStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: GlobalAuditBundleStatus) {
        if (!canMoveTo(next)) {
            error("global audit bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"global audit bundle"} $code"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): GlobalAuditBundle {
        return GlobalAuditBundle(
            code = code,
            title = title,
            pinned = pinned,
            notes = notes,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global audit bundle in status requested. */
fun newGlobalAuditBundle(code: String, title: String): GlobalAuditBundle {
    return GlobalAuditBundle(
        code = code,
        title = title,
        pinned = false,
        notes = "",
        windowDays = 0,
        status = GlobalAuditBundleStatus.REQUESTED,
    )
}
