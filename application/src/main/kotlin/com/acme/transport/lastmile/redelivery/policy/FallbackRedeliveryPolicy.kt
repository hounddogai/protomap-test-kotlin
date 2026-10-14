package com.acme.transport.lastmile.redelivery.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback redelivery policy records of lastmile's redelivery work, and the rules, stores, and
// calculations around them.

/** Where a fallback redelivery policy is in its life cycle. */
enum class FallbackRedeliveryPolicyStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One fallback redelivery policy, with what the planners need to act on it. */
data class FallbackRedeliveryPolicy(
    var code: String,
    var name: String,
    var shiftCode: String,
    var revision: Int,
    var channel: String,
    var attempts: Int,
    var threshold: Double,
    var status: FallbackRedeliveryPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRedeliveryPolicyStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRedeliveryPolicyStatus): Boolean {
        when (status) {
            FallbackRedeliveryPolicyStatus.NEW -> return next == FallbackRedeliveryPolicyStatus.DONE
            FallbackRedeliveryPolicyStatus.QUEUED -> {
                when (next) {
                    FallbackRedeliveryPolicyStatus.RUNNING, FallbackRedeliveryPolicyStatus.FAILED -> return true
                }
                return false
            }
            FallbackRedeliveryPolicyStatus.RUNNING -> return next == FallbackRedeliveryPolicyStatus.DONE
            FallbackRedeliveryPolicyStatus.DONE -> {
                when (next) {
                    FallbackRedeliveryPolicyStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackRedeliveryPolicyStatus) {
        if (!canMoveTo(next)) {
            error("fallback redelivery policy $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code ($name)"

    fun effectiveVolume(): Double = round((attempts.toDouble() - threshold) * 100) / 100

    fun snapshot(): FallbackRedeliveryPolicy {
        return FallbackRedeliveryPolicy(
            code = code,
            name = name,
            shiftCode = shiftCode,
            revision = revision,
            channel = channel,
            attempts = attempts,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fallback redelivery policy in status new. */
fun newFallbackRedeliveryPolicy(code: String, name: String): FallbackRedeliveryPolicy {
    return FallbackRedeliveryPolicy(
        code = code,
        name = name,
        shiftCode = "",
        revision = 0,
        channel = "",
        attempts = 0,
        threshold = 0.0,
        status = FallbackRedeliveryPolicyStatus.NEW,
    )
}
