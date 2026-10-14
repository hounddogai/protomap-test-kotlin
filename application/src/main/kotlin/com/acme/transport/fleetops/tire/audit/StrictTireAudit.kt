package com.acme.transport.fleetops.tire.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict tire audit records of fleetops's tire work, and the rules, stores, and calculations around them.

/** Where a strict tire audit is in its life cycle. */
enum class StrictTireAuditStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A strict tire audit. Its code is unique within a tenant. */
data class StrictTireAudit(
    var code: String,
    var label: String,
    var endsAt: Int,
    var discount: Double,
    var driverId: String,
    var reviewer: String,
    var timezone: String,
    var status: StrictTireAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTireAuditStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTireAuditStatus): Boolean {
        when (status) {
            StrictTireAuditStatus.REQUESTED -> {
                when (next) {
                    StrictTireAuditStatus.GRANTED, StrictTireAuditStatus.REVOKED -> return true
                }
                return false
            }
            StrictTireAuditStatus.GRANTED -> {
                when (next) {
                    StrictTireAuditStatus.REVOKED, StrictTireAuditStatus.EXPIRED -> return true
                }
                return false
            }
            StrictTireAuditStatus.REVOKED -> return next == StrictTireAuditStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: StrictTireAuditStatus) {
        if (!canMoveTo(next)) {
            error("strict tire audit $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveLoad(): Double = round((endsAt.toDouble() - discount) * 100) / 100

    fun snapshot(): StrictTireAudit {
        return StrictTireAudit(
            code = code,
            label = label,
            endsAt = endsAt,
            discount = discount,
            driverId = driverId,
            reviewer = reviewer,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict tire audit in status requested. */
fun newStrictTireAudit(code: String, label: String): StrictTireAudit {
    return StrictTireAudit(
        code = code,
        label = label,
        endsAt = 0,
        discount = 0.0,
        driverId = "",
        reviewer = "",
        timezone = "",
        status = StrictTireAuditStatus.REQUESTED,
    )
}
