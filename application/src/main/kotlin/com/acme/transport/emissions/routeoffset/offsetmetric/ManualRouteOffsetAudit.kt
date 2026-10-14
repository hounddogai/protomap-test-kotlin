package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.kit.text.normalize

/** Where a manual route offset audit is in its life cycle. */
enum class ManualRouteOffsetAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One manual route offset audit, with what the planners need to act on it. */
data class ManualRouteOffsetAudit(
    var code: String,
    var label: String,
    var region: String,
    var pallets: Int,
    var segment: String,
    var status: ManualRouteOffsetAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualRouteOffsetAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualRouteOffsetAuditStatus): Boolean {
        when (status) {
            ManualRouteOffsetAuditStatus.DRAFT -> return next == ManualRouteOffsetAuditStatus.PENDING
            ManualRouteOffsetAuditStatus.PENDING -> return next == ManualRouteOffsetAuditStatus.ACTIVE || next == ManualRouteOffsetAuditStatus.SUSPENDED
            ManualRouteOffsetAuditStatus.ACTIVE -> {
                when (next) {
                    ManualRouteOffsetAuditStatus.SUSPENDED, ManualRouteOffsetAuditStatus.CLOSED -> return true
                }
                return false
            }
            ManualRouteOffsetAuditStatus.SUSPENDED -> return next == ManualRouteOffsetAuditStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ManualRouteOffsetAuditStatus) {
        if (!canMoveTo(next)) {
            error("manual route offset audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"manual route offset audit"} $code"

    fun measuredValue(): Double = pallets.toDouble()

    fun snapshot(): ManualRouteOffsetAudit {
        return ManualRouteOffsetAudit(
            code = code,
            label = label,
            region = region,
            pallets = pallets,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a manual route offset audit in status draft. */
fun newManualRouteOffsetAudit(code: String, label: String): ManualRouteOffsetAudit {
    return ManualRouteOffsetAudit(
        code = code,
        label = label,
        region = "",
        pallets = 0,
        segment = "",
        status = ManualRouteOffsetAuditStatus.DRAFT,
    )
}
