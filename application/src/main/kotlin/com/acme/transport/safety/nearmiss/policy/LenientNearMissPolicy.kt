package com.acme.transport.safety.nearmiss.policy

import com.acme.transport.kit.text.normalize

// The lenient near miss policy records of safety's near miss work, and the rules, stores, and calculations
// around them.

/** Where a lenient near miss policy is in its life cycle. */
enum class LenientNearMissPolicyStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A lenient near miss policy as the near miss team keeps it. */
data class LenientNearMissPolicy(
    var code: String,
    var name: String,
    var budget: Double,
    var region: String,
    var carrier: String,
    var status: LenientNearMissPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientNearMissPolicyStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientNearMissPolicyStatus): Boolean {
        when (status) {
            LenientNearMissPolicyStatus.DRAFT -> {
                when (next) {
                    LenientNearMissPolicyStatus.ACTIVE, LenientNearMissPolicyStatus.SUSPENDED -> return true
                }
                return false
            }
            LenientNearMissPolicyStatus.PENDING -> {
                when (next) {
                    LenientNearMissPolicyStatus.ACTIVE -> return true
                }
                return false
            }
            LenientNearMissPolicyStatus.ACTIVE -> return next == LenientNearMissPolicyStatus.CLOSED
            LenientNearMissPolicyStatus.SUSPENDED -> return next == LenientNearMissPolicyStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientNearMissPolicyStatus) {
        if (!canMoveTo(next)) {
            error("lenient near miss policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun measuredValue(): Double = budget

    fun snapshot(): LenientNearMissPolicy {
        return LenientNearMissPolicy(
            code = code,
            name = name,
            budget = budget,
            region = region,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a lenient near miss policy in status draft. */
fun newLenientNearMissPolicy(code: String, name: String): LenientNearMissPolicy {
    return LenientNearMissPolicy(
        code = code,
        name = name,
        budget = 0.0,
        region = "",
        carrier = "",
        status = LenientNearMissPolicyStatus.DRAFT,
    )
}
