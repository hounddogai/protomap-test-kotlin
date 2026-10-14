package com.acme.transport.freightpay.audit.allocation

import com.acme.transport.kit.text.normalize

// The regional audit allocation records of freightpay's audit work, and the rules, stores, and calculations
// around them.

/** Where a regional audit allocation is in its life cycle. */
enum class RegionalAuditAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A regional audit allocation. Its code is unique within a tenant. */
data class RegionalAuditAllocation(
    var code: String,
    var title: String,
    var summary: String,
    var visible: Boolean,
    var trailerId: String,
    var quantity: Int,
    var status: RegionalAuditAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalAuditAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalAuditAllocationStatus): Boolean {
        when (status) {
            RegionalAuditAllocationStatus.PROPOSED -> return next == RegionalAuditAllocationStatus.APPROVED || next == RegionalAuditAllocationStatus.REJECTED
            RegionalAuditAllocationStatus.APPROVED -> return next == RegionalAuditAllocationStatus.REJECTED
            RegionalAuditAllocationStatus.REJECTED -> return next == RegionalAuditAllocationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RegionalAuditAllocationStatus) {
        if (!canMoveTo(next)) {
            error("regional audit allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional audit allocation"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): RegionalAuditAllocation {
        return RegionalAuditAllocation(
            code = code,
            title = title,
            summary = summary,
            visible = visible,
            trailerId = trailerId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional audit allocation in status proposed. */
fun newRegionalAuditAllocation(code: String, title: String): RegionalAuditAllocation {
    return RegionalAuditAllocation(
        code = code,
        title = title,
        summary = "",
        visible = false,
        trailerId = "",
        quantity = 0,
        status = RegionalAuditAllocationStatus.PROPOSED,
    )
}
