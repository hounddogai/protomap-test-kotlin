package com.acme.transport.fleetops.defect.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled defect audit records of fleetops's defect work, and the rules, stores, and calculations
// around them.

/** Where a scheduled defect audit is in its life cycle. */
enum class ScheduledDefectAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A scheduled defect audit. Its code is unique within a tenant. */
data class ScheduledDefectAudit(
    var code: String,
    var label: String,
    var ownerId: String,
    var segment: String,
    var threshold: Double,
    var contactEmail: String,
    var ceiling: Double,
    var timezone: String,
    var tags: MutableList<String>,
    var locale: String,
    var archived: Boolean,
    var status: ScheduledDefectAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDefectAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDefectAuditStatus): Boolean {
        when (status) {
            ScheduledDefectAuditStatus.DRAFT -> {
                when (next) {
                    ScheduledDefectAuditStatus.SUSPENDED -> return true
                }
                return false
            }
            ScheduledDefectAuditStatus.PENDING -> return next == ScheduledDefectAuditStatus.SUSPENDED || next == ScheduledDefectAuditStatus.CLOSED
            ScheduledDefectAuditStatus.ACTIVE -> return next == ScheduledDefectAuditStatus.SUSPENDED
            ScheduledDefectAuditStatus.SUSPENDED -> return next == ScheduledDefectAuditStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ScheduledDefectAuditStatus) {
        if (!canMoveTo(next)) {
            error("scheduled defect audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"scheduled defect audit"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netExposure(): Double = round((threshold + ceiling) * 100) / 100

    fun snapshot(): ScheduledDefectAudit {
        return ScheduledDefectAudit(
            code = code,
            label = label,
            ownerId = ownerId,
            segment = segment,
            threshold = threshold,
            contactEmail = contactEmail,
            ceiling = ceiling,
            timezone = timezone,
            tags = tags,
            locale = locale,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scheduled defect audit in status draft. */
fun newScheduledDefectAudit(code: String, label: String): ScheduledDefectAudit {
    return ScheduledDefectAudit(
        code = code,
        label = label,
        ownerId = "",
        segment = "",
        threshold = 0.0,
        contactEmail = "",
        ceiling = 0.0,
        timezone = "",
        tags = mutableListOf<String>(),
        locale = "",
        archived = false,
        status = ScheduledDefectAuditStatus.DRAFT,
    )
}
