package com.acme.transport.lastmile.attempt.attemptpolicy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback attempt policy records of lastmile's attempt work, and the rules, stores, and calculations
// around them.

/** Where a fallback attempt policy is in its life cycle. */
enum class FallbackAttemptPolicyStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One fallback attempt policy, with what the planners need to act on it. */
data class FallbackAttemptPolicy(
    var code: String,
    var name: String,
    var checksum: String,
    var retries: Int,
    var etaMinutes: Int,
    var tags: MutableList<String>,
    var locale: String,
    var status: FallbackAttemptPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAttemptPolicyStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAttemptPolicyStatus): Boolean {
        when (status) {
            FallbackAttemptPolicyStatus.OPEN -> {
                when (next) {
                    FallbackAttemptPolicyStatus.REOPENED -> return true
                }
                return false
            }
            FallbackAttemptPolicyStatus.IN_REVIEW -> {
                when (next) {
                    FallbackAttemptPolicyStatus.RESOLVED -> return true
                }
                return false
            }
            FallbackAttemptPolicyStatus.RESOLVED -> {
                when (next) {
                    FallbackAttemptPolicyStatus.REOPENED -> return true
                }
                return false
            }
            FallbackAttemptPolicyStatus.REOPENED -> {
                when (next) {
                    FallbackAttemptPolicyStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackAttemptPolicyStatus) {
        if (!canMoveTo(next)) {
            error("fallback attempt policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedValue(): Double = round((etaMinutes.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): FallbackAttemptPolicy {
        return FallbackAttemptPolicy(
            code = code,
            name = name,
            checksum = checksum,
            retries = retries,
            etaMinutes = etaMinutes,
            tags = tags,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a fallback attempt policy in status open. */
fun newFallbackAttemptPolicy(code: String, name: String): FallbackAttemptPolicy {
    return FallbackAttemptPolicy(
        code = code,
        name = name,
        checksum = "",
        retries = 0,
        etaMinutes = 0,
        tags = mutableListOf<String>(),
        locale = "",
        status = FallbackAttemptPolicyStatus.OPEN,
    )
}
