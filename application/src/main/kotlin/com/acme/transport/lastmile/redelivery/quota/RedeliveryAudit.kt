package com.acme.transport.lastmile.redelivery.quota

import com.acme.transport.kit.text.normalize

/** Where a redelivery audit is in its life cycle. */
enum class RedeliveryAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a redelivery audit and its current status. */
data class RedeliveryAudit(
    var code: String,
    var label: String,
    var rate: Double,
    var trailerId: String,
    var routeCode: String,
    var visible: Boolean,
    var status: RedeliveryAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryAuditStatus): Boolean {
        when (status) {
            RedeliveryAuditStatus.OPEN -> return next == RedeliveryAuditStatus.IN_REVIEW
            RedeliveryAuditStatus.IN_REVIEW -> return next == RedeliveryAuditStatus.RESOLVED
            RedeliveryAuditStatus.RESOLVED -> return next == RedeliveryAuditStatus.ARCHIVED
            RedeliveryAuditStatus.REOPENED -> {
                when (next) {
                    RedeliveryAuditStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryAuditStatus) {
        if (!canMoveTo(next)) {
            error("redelivery audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = rate

    fun snapshot(): RedeliveryAudit {
        return RedeliveryAudit(
            code = code,
            label = label,
            rate = rate,
            trailerId = trailerId,
            routeCode = routeCode,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a redelivery audit in status open. */
fun newRedeliveryAudit(code: String, label: String): RedeliveryAudit {
    return RedeliveryAudit(
        code = code,
        label = label,
        rate = 0.0,
        trailerId = "",
        routeCode = "",
        visible = false,
        status = RedeliveryAuditStatus.OPEN,
    )
}
