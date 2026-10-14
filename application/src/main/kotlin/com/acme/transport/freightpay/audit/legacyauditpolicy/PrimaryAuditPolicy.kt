package com.acme.transport.freightpay.audit.legacyauditpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary audit policy records of freightpay's audit work, and the rules, stores, and calculations around
// them.

/** Where a primary audit policy is in its life cycle. */
enum class PrimaryAuditPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary audit policy, with what the planners need to act on it. */
data class PrimaryAuditPolicy(
    var code: String,
    var title: String,
    var approved: Boolean,
    var updatedAt: Int,
    var odometer: Int,
    var description: String,
    var priority: Int,
    var version: Int,
    var status: PrimaryAuditPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAuditPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAuditPolicyStatus): Boolean {
        when (status) {
            PrimaryAuditPolicyStatus.OPEN -> return next == PrimaryAuditPolicyStatus.IN_REVIEW || next == PrimaryAuditPolicyStatus.ARCHIVED
            PrimaryAuditPolicyStatus.IN_REVIEW -> return next == PrimaryAuditPolicyStatus.REOPENED || next == PrimaryAuditPolicyStatus.ARCHIVED
            PrimaryAuditPolicyStatus.RESOLVED -> {
                when (next) {
                    PrimaryAuditPolicyStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryAuditPolicyStatus.REOPENED -> return next == PrimaryAuditPolicyStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: PrimaryAuditPolicyStatus) {
        if (!canMoveTo(next)) {
            error("primary audit policy $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun effectiveVolume(): Double = round(odometer.toDouble() * updatedAt.toDouble() * 100) / 100

    fun snapshot(): PrimaryAuditPolicy {
        return PrimaryAuditPolicy(
            code = code,
            title = title,
            approved = approved,
            updatedAt = updatedAt,
            odometer = odometer,
            description = description,
            priority = priority,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a primary audit policy in status open. */
fun newPrimaryAuditPolicy(code: String, title: String): PrimaryAuditPolicy {
    return PrimaryAuditPolicy(
        code = code,
        title = title,
        approved = false,
        updatedAt = 0,
        odometer = 0,
        description = "",
        priority = 0,
        version = 0,
        status = PrimaryAuditPolicyStatus.OPEN,
    )
}
