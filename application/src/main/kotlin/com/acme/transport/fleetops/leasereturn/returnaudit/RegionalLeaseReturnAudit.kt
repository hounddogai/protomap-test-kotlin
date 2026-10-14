package com.acme.transport.fleetops.leasereturn.returnaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional lease return audit records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a regional lease return audit is in its life cycle. */
enum class RegionalLeaseReturnAuditStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional lease return audit as the lease return team keeps it. */
data class RegionalLeaseReturnAudit(
    var code: String,
    var name: String,
    var trailerId: String,
    var attempts: Int,
    var lane: String,
    var source: String,
    var channel: String,
    var graceDays: Int,
    var status: RegionalLeaseReturnAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalLeaseReturnAuditStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalLeaseReturnAuditStatus): Boolean {
        when (status) {
            RegionalLeaseReturnAuditStatus.DRAFT -> return next == RegionalLeaseReturnAuditStatus.PENDING || next == RegionalLeaseReturnAuditStatus.ACTIVE
            RegionalLeaseReturnAuditStatus.PENDING -> {
                when (next) {
                    RegionalLeaseReturnAuditStatus.ACTIVE -> return true
                }
                return false
            }
            RegionalLeaseReturnAuditStatus.ACTIVE -> {
                when (next) {
                    RegionalLeaseReturnAuditStatus.SUSPENDED, RegionalLeaseReturnAuditStatus.CLOSED -> return true
                }
                return false
            }
            RegionalLeaseReturnAuditStatus.SUSPENDED -> {
                when (next) {
                    RegionalLeaseReturnAuditStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalLeaseReturnAuditStatus) {
        if (!canMoveTo(next)) {
            error("regional lease return audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional lease return audit"} $code"

    fun projectedLoad(): Double = round(attempts.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): RegionalLeaseReturnAudit {
        return RegionalLeaseReturnAudit(
            code = code,
            name = name,
            trailerId = trailerId,
            attempts = attempts,
            lane = lane,
            source = source,
            channel = channel,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a regional lease return audit in status draft. */
fun newRegionalLeaseReturnAudit(code: String, name: String): RegionalLeaseReturnAudit {
    return RegionalLeaseReturnAudit(
        code = code,
        name = name,
        trailerId = "",
        attempts = 0,
        lane = "",
        source = "",
        channel = "",
        graceDays = 0,
        status = RegionalLeaseReturnAuditStatus.DRAFT,
    )
}
