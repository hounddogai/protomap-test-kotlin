package com.acme.transport.freightpay.audit.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The audit policy records of freightpay's audit work, and the rules, stores, and calculations around them.

/** Where a audit policy is in its life cycle. */
enum class AuditPolicyStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One audit policy, with what the planners need to act on it. */
data class AuditPolicy(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var minimum: Double,
    var trailerId: String,
    var currency: String,
    var status: AuditPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AuditPolicyStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AuditPolicyStatus): Boolean {
        when (status) {
            AuditPolicyStatus.PLANNED -> return next == AuditPolicyStatus.COMPLETED
            AuditPolicyStatus.SCHEDULED -> {
                when (next) {
                    AuditPolicyStatus.COMPLETED -> return true
                }
                return false
            }
            AuditPolicyStatus.IN_PROGRESS -> {
                when (next) {
                    AuditPolicyStatus.COMPLETED, AuditPolicyStatus.CANCELLED -> return true
                }
                return false
            }
            AuditPolicyStatus.COMPLETED -> {
                when (next) {
                    AuditPolicyStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AuditPolicyStatus) {
        if (!canMoveTo(next)) {
            error("audit policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedTotal(): Double = round((minimum + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): AuditPolicy {
        return AuditPolicy(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            minimum = minimum,
            trailerId = trailerId,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a audit policy in status planned. */
fun newAuditPolicy(code: String, title: String): AuditPolicy {
    return AuditPolicy(
        code = code,
        title = title,
        durationMinutes = 0,
        minimum = 0.0,
        trailerId = "",
        currency = "",
        status = AuditPolicyStatus.PLANNED,
    )
}
