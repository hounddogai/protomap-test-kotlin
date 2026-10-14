package com.acme.transport.network.transittime.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a transit time policy is in its life cycle. */
enum class TransitTimePolicyStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A transit time policy as the transit time team keeps it. */
data class TransitTimePolicy(
    var code: String,
    var title: String,
    var routeCode: String,
    var stopCount: Int,
    var budget: Double,
    var status: TransitTimePolicyStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimePolicyStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimePolicyStatus): Boolean {
        when (status) {
            TransitTimePolicyStatus.PROPOSED -> {
                when (next) {
                    TransitTimePolicyStatus.APPROVED, TransitTimePolicyStatus.REJECTED -> return true
                }
                return false
            }
            TransitTimePolicyStatus.APPROVED -> {
                when (next) {
                    TransitTimePolicyStatus.REJECTED, TransitTimePolicyStatus.WITHDRAWN -> return true
                }
                return false
            }
            TransitTimePolicyStatus.REJECTED -> return next == TransitTimePolicyStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: TransitTimePolicyStatus) {
        if (!canMoveTo(next)) {
            error("transit time policy $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"transit time policy"} $code"

    fun adjustedValue(): Double = round(budget * stopCount.toDouble() * 100) / 100

    fun snapshot(): TransitTimePolicy {
        return TransitTimePolicy(
            code = code,
            title = title,
            routeCode = routeCode,
            stopCount = stopCount,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a transit time policy in status proposed. */
fun newTransitTimePolicy(code: String, title: String): TransitTimePolicy {
    return TransitTimePolicy(
        code = code,
        title = title,
        routeCode = "",
        stopCount = 0,
        budget = 0.0,
        status = TransitTimePolicyStatus.PROPOSED,
    )
}
