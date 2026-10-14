package com.acme.transport.lastmile.attempt.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The attempt policy records of lastmile's attempt work, and the rules, stores, and calculations around them.

/** Where a attempt policy is in its life cycle. */
enum class AttemptPolicyStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A attempt policy. Its code is unique within a tenant. */
data class AttemptPolicy(
    var code: String,
    var name: String,
    var pinned: Boolean,
    var category: String,
    var approved: Boolean,
    var durationMinutes: Int,
    var windowDays: Int,
    var amount: Double,
    var status: AttemptPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptPolicyStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptPolicyStatus): Boolean {
        when (status) {
            AttemptPolicyStatus.REQUESTED -> {
                when (next) {
                    AttemptPolicyStatus.REVOKED -> return true
                }
                return false
            }
            AttemptPolicyStatus.GRANTED -> return next == AttemptPolicyStatus.REVOKED || next == AttemptPolicyStatus.EXPIRED
            AttemptPolicyStatus.REVOKED -> return next == AttemptPolicyStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AttemptPolicyStatus) {
        if (!canMoveTo(next)) {
            error("attempt policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun netExposure(): Double = round((durationMinutes.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): AttemptPolicy {
        return AttemptPolicy(
            code = code,
            name = name,
            pinned = pinned,
            category = category,
            approved = approved,
            durationMinutes = durationMinutes,
            windowDays = windowDays,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a attempt policy in status requested. */
fun newAttemptPolicy(code: String, name: String): AttemptPolicy {
    return AttemptPolicy(
        code = code,
        name = name,
        pinned = false,
        category = "",
        approved = false,
        durationMinutes = 0,
        windowDays = 0,
        amount = 0.0,
        status = AttemptPolicyStatus.REQUESTED,
    )
}
