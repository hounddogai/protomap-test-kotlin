package com.acme.transport.linehaul.relay.relaypolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback relay policy records of linehaul's relay work, and the rules, stores, and calculations around
// them.

/** Where a fallback relay policy is in its life cycle. */
enum class FallbackRelayPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One fallback relay policy, with what the planners need to act on it. */
data class FallbackRelayPolicy(
    var code: String,
    var title: String,
    var threshold: Double,
    var phone: String,
    var durationMinutes: Int,
    var ceiling: Double,
    var priority: Int,
    var score: Double,
    var status: FallbackRelayPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRelayPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRelayPolicyStatus): Boolean {
        when (status) {
            FallbackRelayPolicyStatus.OPEN -> return next == FallbackRelayPolicyStatus.REOPENED
            FallbackRelayPolicyStatus.IN_REVIEW -> return next == FallbackRelayPolicyStatus.ARCHIVED
            FallbackRelayPolicyStatus.RESOLVED -> return next == FallbackRelayPolicyStatus.REOPENED || next == FallbackRelayPolicyStatus.ARCHIVED
            FallbackRelayPolicyStatus.REOPENED -> {
                when (next) {
                    FallbackRelayPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackRelayPolicyStatus) {
        if (!canMoveTo(next)) {
            error("fallback relay policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback relay policy"} $code"

    fun effectiveExposure(): Double = round((priority.toDouble() + ceiling) * 100) / 100

    fun snapshot(): FallbackRelayPolicy {
        return FallbackRelayPolicy(
            code = code,
            title = title,
            threshold = threshold,
            phone = phone,
            durationMinutes = durationMinutes,
            ceiling = ceiling,
            priority = priority,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback relay policy in status open. */
fun newFallbackRelayPolicy(code: String, title: String): FallbackRelayPolicy {
    return FallbackRelayPolicy(
        code = code,
        title = title,
        threshold = 0.0,
        phone = "",
        durationMinutes = 0,
        ceiling = 0.0,
        priority = 0,
        score = 0.0,
        status = FallbackRelayPolicyStatus.OPEN,
    )
}
