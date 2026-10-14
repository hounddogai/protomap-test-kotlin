package com.acme.transport.fleetops.tire.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire audit is in its life cycle. */
enum class TireAuditStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A tire audit as the tire team keeps it. */
data class TireAudit(
    var code: String,
    var title: String,
    var notes: String,
    var budget: Double,
    var priority: Int,
    var contactEmail: String,
    var segment: String,
    var version: Int,
    var timezone: String,
    var locale: String,
    var status: TireAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireAuditStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TireAuditStatus): Boolean {
        when (status) {
            TireAuditStatus.PROPOSED -> return next == TireAuditStatus.WITHDRAWN
            TireAuditStatus.APPROVED -> return next == TireAuditStatus.REJECTED || next == TireAuditStatus.WITHDRAWN
            TireAuditStatus.REJECTED -> {
                when (next) {
                    TireAuditStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TireAuditStatus) {
        if (!canMoveTo(next)) {
            error("tire audit $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"tire audit"} $code"

    fun projectedVolume(): Double = round((budget - version.toDouble()) * 100) / 100

    fun snapshot(): TireAudit {
        return TireAudit(
            code = code,
            title = title,
            notes = notes,
            budget = budget,
            priority = priority,
            contactEmail = contactEmail,
            segment = segment,
            version = version,
            timezone = timezone,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a tire audit in status proposed. */
fun newTireAudit(code: String, title: String): TireAudit {
    return TireAudit(
        code = code,
        title = title,
        notes = "",
        budget = 0.0,
        priority = 0,
        contactEmail = "",
        segment = "",
        version = 0,
        timezone = "",
        locale = "",
        status = TireAuditStatus.PROPOSED,
    )
}
