package com.acme.transport.lastmile.redelivery.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The redelivery override records of lastmile's redelivery work, and the rules, stores, and calculations
// around them.

/** Where a redelivery override is in its life cycle. */
enum class RedeliveryOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a redelivery override and its current status. */
data class RedeliveryOverride(
    var code: String,
    var label: String,
    var limit: Int,
    var cost: Double,
    var title: String,
    var carrier: String,
    var source: String,
    var expiresAt: Int,
    var amount: Double,
    var graceDays: Int,
    var status: RedeliveryOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryOverrideStatus): Boolean {
        when (status) {
            RedeliveryOverrideStatus.IDLE -> {
                when (next) {
                    RedeliveryOverrideStatus.DRAINING -> return true
                }
                return false
            }
            RedeliveryOverrideStatus.WARMING -> {
                when (next) {
                    RedeliveryOverrideStatus.READY, RedeliveryOverrideStatus.STOPPED -> return true
                }
                return false
            }
            RedeliveryOverrideStatus.READY -> return next == RedeliveryOverrideStatus.DRAINING
            RedeliveryOverrideStatus.DRAINING -> return next == RedeliveryOverrideStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RedeliveryOverrideStatus) {
        if (!canMoveTo(next)) {
            error("redelivery override $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedTotal(): Double = round((graceDays.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryOverride {
        return RedeliveryOverride(
            code = code,
            label = label,
            limit = limit,
            cost = cost,
            title = title,
            carrier = carrier,
            source = source,
            expiresAt = expiresAt,
            amount = amount,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a redelivery override in status idle. */
fun newRedeliveryOverride(code: String, label: String): RedeliveryOverride {
    return RedeliveryOverride(
        code = code,
        label = label,
        limit = 0,
        cost = 0.0,
        title = "",
        carrier = "",
        source = "",
        expiresAt = 0,
        amount = 0.0,
        graceDays = 0,
        status = RedeliveryOverrideStatus.IDLE,
    )
}
