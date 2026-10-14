package com.acme.transport.fleetops.leasereturn.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient lease return audit records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a lenient lease return audit is in its life cycle. */
enum class LenientLeaseReturnAuditStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A lenient lease return audit as the lease return team keeps it. */
data class LenientLeaseReturnAudit(
    var code: String,
    var title: String,
    var driverId: String,
    var batchSize: Int,
    var active: Boolean,
    var cost: Double,
    var depot: String,
    var contactEmail: String,
    var status: LenientLeaseReturnAuditStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientLeaseReturnAuditStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientLeaseReturnAuditStatus): Boolean {
        when (status) {
            LenientLeaseReturnAuditStatus.NEW -> return next == LenientLeaseReturnAuditStatus.QUEUED
            LenientLeaseReturnAuditStatus.QUEUED -> {
                when (next) {
                    LenientLeaseReturnAuditStatus.RUNNING, LenientLeaseReturnAuditStatus.DONE -> return true
                }
                return false
            }
            LenientLeaseReturnAuditStatus.RUNNING -> return next == LenientLeaseReturnAuditStatus.DONE
            LenientLeaseReturnAuditStatus.DONE -> return next == LenientLeaseReturnAuditStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LenientLeaseReturnAuditStatus) {
        if (!canMoveTo(next)) {
            error("lenient lease return audit $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun projectedTotal(): Double = round((cost + batchSize.toDouble()) * 100) / 100

    fun snapshot(): LenientLeaseReturnAudit {
        return LenientLeaseReturnAudit(
            code = code,
            title = title,
            driverId = driverId,
            batchSize = batchSize,
            active = active,
            cost = cost,
            depot = depot,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a lenient lease return audit in status new. */
fun newLenientLeaseReturnAudit(code: String, title: String): LenientLeaseReturnAudit {
    return LenientLeaseReturnAudit(
        code = code,
        title = title,
        driverId = "",
        batchSize = 0,
        active = false,
        cost = 0.0,
        depot = "",
        contactEmail = "",
        status = LenientLeaseReturnAuditStatus.NEW,
    )
}
