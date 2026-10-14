package com.acme.transport.lastmile.attempt.defaultattemptpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy attempt policy records of lastmile's attempt work, and the rules, stores, and calculations
// around them.

/** Where a legacy attempt policy is in its life cycle. */
enum class LegacyAttemptPolicyStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a legacy attempt policy and its current status. */
data class LegacyAttemptPolicy(
    var code: String,
    var label: String,
    var ceiling: Double,
    var fuelLiters: Double,
    var name: String,
    var status: LegacyAttemptPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyAttemptPolicyStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyAttemptPolicyStatus): Boolean {
        when (status) {
            LegacyAttemptPolicyStatus.DRAFT -> return next == LegacyAttemptPolicyStatus.SUSPENDED
            LegacyAttemptPolicyStatus.PENDING -> return next == LegacyAttemptPolicyStatus.SUSPENDED
            LegacyAttemptPolicyStatus.ACTIVE -> {
                when (next) {
                    LegacyAttemptPolicyStatus.SUSPENDED -> return true
                }
                return false
            }
            LegacyAttemptPolicyStatus.SUSPENDED -> return next == LegacyAttemptPolicyStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LegacyAttemptPolicyStatus) {
        if (!canMoveTo(next)) {
            error("legacy attempt policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun projectedLoad(): Double = round((ceiling + fuelLiters) * 100) / 100

    fun snapshot(): LegacyAttemptPolicy {
        return LegacyAttemptPolicy(
            code = code,
            label = label,
            ceiling = ceiling,
            fuelLiters = fuelLiters,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy attempt policy in status draft. */
fun newLegacyAttemptPolicy(code: String, label: String): LegacyAttemptPolicy {
    return LegacyAttemptPolicy(
        code = code,
        label = label,
        ceiling = 0.0,
        fuelLiters = 0.0,
        name = "",
        status = LegacyAttemptPolicyStatus.DRAFT,
    )
}
