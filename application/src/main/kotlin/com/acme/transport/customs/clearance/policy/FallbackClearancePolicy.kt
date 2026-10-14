package com.acme.transport.customs.clearance.policy

import com.acme.transport.kit.text.normalize

// The fallback clearance policy records of customs's clearance work, and the rules, stores, and calculations
// around them.

/** Where a fallback clearance policy is in its life cycle. */
enum class FallbackClearancePolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback clearance policy. Its code is unique within a tenant. */
data class FallbackClearancePolicy(
    var code: String,
    var title: String,
    var pinned: Boolean,
    var trailerId: String,
    var locale: String,
    var score: Double,
    var status: FallbackClearancePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackClearancePolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackClearancePolicyStatus): Boolean {
        when (status) {
            FallbackClearancePolicyStatus.OPEN -> {
                when (next) {
                    FallbackClearancePolicyStatus.IN_REVIEW, FallbackClearancePolicyStatus.REOPENED -> return true
                }
                return false
            }
            FallbackClearancePolicyStatus.IN_REVIEW -> return next == FallbackClearancePolicyStatus.REOPENED || next == FallbackClearancePolicyStatus.ARCHIVED
            FallbackClearancePolicyStatus.RESOLVED -> return next == FallbackClearancePolicyStatus.REOPENED
            FallbackClearancePolicyStatus.REOPENED -> return next == FallbackClearancePolicyStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackClearancePolicyStatus) {
        if (!canMoveTo(next)) {
            error("fallback clearance policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = score

    fun snapshot(): FallbackClearancePolicy {
        return FallbackClearancePolicy(
            code = code,
            title = title,
            pinned = pinned,
            trailerId = trailerId,
            locale = locale,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback clearance policy in status open. */
fun newFallbackClearancePolicy(code: String, title: String): FallbackClearancePolicy {
    return FallbackClearancePolicy(
        code = code,
        title = title,
        pinned = false,
        trailerId = "",
        locale = "",
        score = 0.0,
        status = FallbackClearancePolicyStatus.OPEN,
    )
}
