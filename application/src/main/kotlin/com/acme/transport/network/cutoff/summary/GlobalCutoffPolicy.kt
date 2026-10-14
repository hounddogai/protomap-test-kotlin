package com.acme.transport.network.cutoff.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global cutoff policy is in its life cycle. */
enum class GlobalCutoffPolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One global cutoff policy, with what the planners need to act on it. */
data class GlobalCutoffPolicy(
    var code: String,
    var label: String,
    var routeCode: String,
    var discount: Double,
    var contactEmail: String,
    var rate: Double,
    var createdAt: Int,
    var attempts: Int,
    var startsAt: Int,
    var margin: Double,
    var segment: String,
    var status: GlobalCutoffPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCutoffPolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCutoffPolicyStatus): Boolean {
        when (status) {
            GlobalCutoffPolicyStatus.PROPOSED -> {
                when (next) {
                    GlobalCutoffPolicyStatus.APPROVED -> return true
                }
                return false
            }
            GlobalCutoffPolicyStatus.APPROVED -> return next == GlobalCutoffPolicyStatus.REJECTED
            GlobalCutoffPolicyStatus.REJECTED -> {
                when (next) {
                    GlobalCutoffPolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCutoffPolicyStatus) {
        if (!canMoveTo(next)) {
            error("global cutoff policy $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedValue(): Double = round(margin * startsAt.toDouble() * 100) / 100

    fun snapshot(): GlobalCutoffPolicy {
        return GlobalCutoffPolicy(
            code = code,
            label = label,
            routeCode = routeCode,
            discount = discount,
            contactEmail = contactEmail,
            rate = rate,
            createdAt = createdAt,
            attempts = attempts,
            startsAt = startsAt,
            margin = margin,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a global cutoff policy in status proposed. */
fun newGlobalCutoffPolicy(code: String, label: String): GlobalCutoffPolicy {
    return GlobalCutoffPolicy(
        code = code,
        label = label,
        routeCode = "",
        discount = 0.0,
        contactEmail = "",
        rate = 0.0,
        createdAt = 0,
        attempts = 0,
        startsAt = 0,
        margin = 0.0,
        segment = "",
        status = GlobalCutoffPolicyStatus.PROPOSED,
    )
}
