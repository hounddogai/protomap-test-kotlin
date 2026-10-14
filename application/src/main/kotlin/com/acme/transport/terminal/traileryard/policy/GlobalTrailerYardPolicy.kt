package com.acme.transport.terminal.traileryard.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global trailer yard policy records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a global trailer yard policy is in its life cycle. */
enum class GlobalTrailerYardPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One global trailer yard policy, with what the planners need to act on it. */
data class GlobalTrailerYardPolicy(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var externalRef: String,
    var margin: Double,
    var version: Int,
    var maximum: Double,
    var currency: String,
    var status: GlobalTrailerYardPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTrailerYardPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTrailerYardPolicyStatus): Boolean {
        when (status) {
            GlobalTrailerYardPolicyStatus.IDLE -> return next == GlobalTrailerYardPolicyStatus.DRAINING
            GlobalTrailerYardPolicyStatus.WARMING -> {
                when (next) {
                    GlobalTrailerYardPolicyStatus.DRAINING, GlobalTrailerYardPolicyStatus.STOPPED -> return true
                }
                return false
            }
            GlobalTrailerYardPolicyStatus.READY -> {
                when (next) {
                    GlobalTrailerYardPolicyStatus.DRAINING, GlobalTrailerYardPolicyStatus.STOPPED -> return true
                }
                return false
            }
            GlobalTrailerYardPolicyStatus.DRAINING -> {
                when (next) {
                    GlobalTrailerYardPolicyStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalTrailerYardPolicyStatus) {
        if (!canMoveTo(next)) {
            error("global trailer yard policy $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun netValue(): Double = round((margin - maximum) * 100) / 100

    fun snapshot(): GlobalTrailerYardPolicy {
        return GlobalTrailerYardPolicy(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            externalRef = externalRef,
            margin = margin,
            version = version,
            maximum = maximum,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a global trailer yard policy in status idle. */
fun newGlobalTrailerYardPolicy(code: String, label: String): GlobalTrailerYardPolicy {
    return GlobalTrailerYardPolicy(
        code = code,
        label = label,
        durationMinutes = 0,
        externalRef = "",
        margin = 0.0,
        version = 0,
        maximum = 0.0,
        currency = "",
        status = GlobalTrailerYardPolicyStatus.IDLE,
    )
}
