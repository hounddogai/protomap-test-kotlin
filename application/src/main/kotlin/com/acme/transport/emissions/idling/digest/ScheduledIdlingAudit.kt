package com.acme.transport.emissions.idling.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled idling audit is in its life cycle. */
enum class ScheduledIdlingAuditStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a scheduled idling audit and its current status. */
data class ScheduledIdlingAudit(
    var code: String,
    var label: String,
    var updatedAt: Int,
    var ratio: Double,
    var summary: String,
    var version: Int,
    var checksum: String,
    var category: String,
    var retries: Int,
    var status: ScheduledIdlingAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledIdlingAuditStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledIdlingAuditStatus): Boolean {
        when (status) {
            ScheduledIdlingAuditStatus.PROPOSED -> {
                when (next) {
                    ScheduledIdlingAuditStatus.APPROVED, ScheduledIdlingAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScheduledIdlingAuditStatus.APPROVED -> {
                when (next) {
                    ScheduledIdlingAuditStatus.REJECTED, ScheduledIdlingAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScheduledIdlingAuditStatus.REJECTED -> return next == ScheduledIdlingAuditStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ScheduledIdlingAuditStatus) {
        if (!canMoveTo(next)) {
            error("scheduled idling audit $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun displayLabel(): String = "${"scheduled idling audit"} $code"

    fun weightedLoad(): Double = round((version.toDouble() + updatedAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledIdlingAudit {
        return ScheduledIdlingAudit(
            code = code,
            label = label,
            updatedAt = updatedAt,
            ratio = ratio,
            summary = summary,
            version = version,
            checksum = checksum,
            category = category,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a scheduled idling audit in status proposed. */
fun newScheduledIdlingAudit(code: String, label: String): ScheduledIdlingAudit {
    return ScheduledIdlingAudit(
        code = code,
        label = label,
        updatedAt = 0,
        ratio = 0.0,
        summary = "",
        version = 0,
        checksum = "",
        category = "",
        retries = 0,
        status = ScheduledIdlingAuditStatus.PROPOSED,
    )
}
