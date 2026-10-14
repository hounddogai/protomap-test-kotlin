package com.acme.transport.fleetops.utilization.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted utilization audit records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a weighted utilization audit is in its life cycle. */
enum class WeightedUtilizationAuditStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted utilization audit as the utilization team keeps it. */
data class WeightedUtilizationAudit(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var summary: String,
    var version: Int,
    var stopCount: Int,
    var graceDays: Int,
    var notes: String,
    var priority: Int,
    var ownerId: String,
    var status: WeightedUtilizationAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedUtilizationAuditStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedUtilizationAuditStatus): Boolean {
        when (status) {
            WeightedUtilizationAuditStatus.OPEN -> return next == WeightedUtilizationAuditStatus.RESOLVED || next == WeightedUtilizationAuditStatus.REOPENED
            WeightedUtilizationAuditStatus.IN_REVIEW -> {
                when (next) {
                    WeightedUtilizationAuditStatus.REOPENED -> return true
                }
                return false
            }
            WeightedUtilizationAuditStatus.RESOLVED -> return next == WeightedUtilizationAuditStatus.REOPENED || next == WeightedUtilizationAuditStatus.ARCHIVED
            WeightedUtilizationAuditStatus.REOPENED -> {
                when (next) {
                    WeightedUtilizationAuditStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedUtilizationAuditStatus) {
        if (!canMoveTo(next)) {
            error("weighted utilization audit $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($title)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedValue(): Double = round((stopCount.toDouble() - priority.toDouble()) * 100) / 100

    fun snapshot(): WeightedUtilizationAudit {
        return WeightedUtilizationAudit(
            code = code,
            title = title,
            tags = tags,
            summary = summary,
            version = version,
            stopCount = stopCount,
            graceDays = graceDays,
            notes = notes,
            priority = priority,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted utilization audit in status open. */
fun newWeightedUtilizationAudit(code: String, title: String): WeightedUtilizationAudit {
    return WeightedUtilizationAudit(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        summary = "",
        version = 0,
        stopCount = 0,
        graceDays = 0,
        notes = "",
        priority = 0,
        ownerId = "",
        status = WeightedUtilizationAuditStatus.OPEN,
    )
}
