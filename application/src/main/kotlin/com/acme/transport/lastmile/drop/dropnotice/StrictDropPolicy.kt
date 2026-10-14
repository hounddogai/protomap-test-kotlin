package com.acme.transport.lastmile.drop.dropnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict drop policy is in its life cycle. */
enum class StrictDropPolicyStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One strict drop policy, with what the planners need to act on it. */
data class StrictDropPolicy(
    var code: String,
    var name: String,
    var externalRef: String,
    var limit: Int,
    var capacity: Int,
    var minimum: Double,
    var country: String,
    var amount: Double,
    var status: StrictDropPolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDropPolicyStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDropPolicyStatus): Boolean {
        when (status) {
            StrictDropPolicyStatus.IDLE -> return next == StrictDropPolicyStatus.DRAINING || next == StrictDropPolicyStatus.STOPPED
            StrictDropPolicyStatus.WARMING -> {
                when (next) {
                    StrictDropPolicyStatus.STOPPED -> return true
                }
                return false
            }
            StrictDropPolicyStatus.READY -> return next == StrictDropPolicyStatus.DRAINING || next == StrictDropPolicyStatus.STOPPED
            StrictDropPolicyStatus.DRAINING -> {
                when (next) {
                    StrictDropPolicyStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictDropPolicyStatus) {
        if (!canMoveTo(next)) {
            error("strict drop policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict drop policy"} $code"

    fun netLoad(): Double = round((limit.toDouble() - minimum) * 100) / 100

    fun snapshot(): StrictDropPolicy {
        return StrictDropPolicy(
            code = code,
            name = name,
            externalRef = externalRef,
            limit = limit,
            capacity = capacity,
            minimum = minimum,
            country = country,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a strict drop policy in status idle. */
fun newStrictDropPolicy(code: String, name: String): StrictDropPolicy {
    return StrictDropPolicy(
        code = code,
        name = name,
        externalRef = "",
        limit = 0,
        capacity = 0,
        minimum = 0.0,
        country = "",
        amount = 0.0,
        status = StrictDropPolicyStatus.IDLE,
    )
}
