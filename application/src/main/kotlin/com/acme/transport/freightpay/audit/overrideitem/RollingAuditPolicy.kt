package com.acme.transport.freightpay.audit.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling audit policy is in its life cycle. */
enum class RollingAuditPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A rolling audit policy. Its code is unique within a tenant. */
data class RollingAuditPolicy(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var cost: Double,
    var expiresAt: Int,
    var reviewer: String,
    var approved: Boolean,
    var status: RollingAuditPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAuditPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAuditPolicyStatus): Boolean {
        when (status) {
            RollingAuditPolicyStatus.REQUESTED -> {
                when (next) {
                    RollingAuditPolicyStatus.GRANTED, RollingAuditPolicyStatus.REVOKED -> return true
                }
                return false
            }
            RollingAuditPolicyStatus.GRANTED -> return next == RollingAuditPolicyStatus.EXPIRED
            RollingAuditPolicyStatus.REVOKED -> return next == RollingAuditPolicyStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: RollingAuditPolicyStatus) {
        if (!canMoveTo(next)) {
            error("rolling audit policy $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedVolume(): Double = round(cost * expiresAt.toDouble() * 100) / 100

    fun snapshot(): RollingAuditPolicy {
        return RollingAuditPolicy(
            code = code,
            label = label,
            pinned = pinned,
            cost = cost,
            expiresAt = expiresAt,
            reviewer = reviewer,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling audit policy in status requested. */
fun newRollingAuditPolicy(code: String, label: String): RollingAuditPolicy {
    return RollingAuditPolicy(
        code = code,
        label = label,
        pinned = false,
        cost = 0.0,
        expiresAt = 0,
        reviewer = "",
        approved = false,
        status = RollingAuditPolicyStatus.REQUESTED,
    )
}
