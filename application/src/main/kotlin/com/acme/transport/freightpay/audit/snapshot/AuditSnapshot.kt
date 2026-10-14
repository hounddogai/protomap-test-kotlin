package com.acme.transport.freightpay.audit.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The audit snapshot records of freightpay's audit work, and the rules, stores, and calculations around them.

/** Where a audit snapshot is in its life cycle. */
enum class AuditSnapshotStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A audit snapshot as the audit team keeps it. */
data class AuditSnapshot(
    var code: String,
    var name: String,
    var locale: String,
    var maximum: Double,
    var updatedAt: Int,
    var shiftCode: String,
    var segment: String,
    var country: String,
    var status: AuditSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditSnapshotStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditSnapshotStatus): Boolean {
        when (status) {
            AuditSnapshotStatus.PROPOSED -> {
                when (next) {
                    AuditSnapshotStatus.APPROVED, AuditSnapshotStatus.WITHDRAWN -> return true
                }
                return false
            }
            AuditSnapshotStatus.APPROVED -> {
                when (next) {
                    AuditSnapshotStatus.REJECTED -> return true
                }
                return false
            }
            AuditSnapshotStatus.REJECTED -> return next == AuditSnapshotStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AuditSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("audit snapshot $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"audit snapshot"} $code"

    fun weightedValue(): Double = round(maximum * updatedAt.toDouble() * 100) / 100

    fun snapshot(): AuditSnapshot {
        return AuditSnapshot(
            code = code,
            name = name,
            locale = locale,
            maximum = maximum,
            updatedAt = updatedAt,
            shiftCode = shiftCode,
            segment = segment,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a audit snapshot in status proposed. */
fun newAuditSnapshot(code: String, name: String): AuditSnapshot {
    return AuditSnapshot(
        code = code,
        name = name,
        locale = "",
        maximum = 0.0,
        updatedAt = 0,
        shiftCode = "",
        segment = "",
        country = "",
        status = AuditSnapshotStatus.PROPOSED,
    )
}
