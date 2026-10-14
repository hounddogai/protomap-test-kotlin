package com.acme.transport.network.hub.audit

import com.acme.transport.kit.text.normalize

// The scheduled hub audit records of network's hub work, and the rules, stores, and calculations around them.

/** Where a scheduled hub audit is in its life cycle. */
enum class ScheduledHubAuditStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a scheduled hub audit and its current status. */
data class ScheduledHubAudit(
    var code: String,
    var label: String,
    var routeCode: String,
    var pinned: Boolean,
    var externalRef: String,
    var threshold: Double,
    var status: ScheduledHubAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledHubAuditStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledHubAuditStatus): Boolean {
        when (status) {
            ScheduledHubAuditStatus.IDLE -> {
                when (next) {
                    ScheduledHubAuditStatus.READY -> return true
                }
                return false
            }
            ScheduledHubAuditStatus.WARMING -> {
                when (next) {
                    ScheduledHubAuditStatus.DRAINING, ScheduledHubAuditStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledHubAuditStatus.READY -> {
                when (next) {
                    ScheduledHubAuditStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledHubAuditStatus.DRAINING -> {
                when (next) {
                    ScheduledHubAuditStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledHubAuditStatus) {
        if (!canMoveTo(next)) {
            error("scheduled hub audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = threshold

    fun snapshot(): ScheduledHubAudit {
        return ScheduledHubAudit(
            code = code,
            label = label,
            routeCode = routeCode,
            pinned = pinned,
            externalRef = externalRef,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled hub audit in status idle. */
fun newScheduledHubAudit(code: String, label: String): ScheduledHubAudit {
    return ScheduledHubAudit(
        code = code,
        label = label,
        routeCode = "",
        pinned = false,
        externalRef = "",
        threshold = 0.0,
        status = ScheduledHubAuditStatus.IDLE,
    )
}
