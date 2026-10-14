package com.acme.transport.lastmile.redelivery.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The redelivery baseline records of lastmile's redelivery work, and the rules, stores, and calculations
// around them.

/** Where a redelivery baseline is in its life cycle. */
enum class RedeliveryBaselineStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One redelivery baseline, with what the planners need to act on it. */
data class RedeliveryBaseline(
    var code: String,
    var name: String,
    var graceDays: Int,
    var vehicleId: String,
    var weight: Double,
    var status: RedeliveryBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryBaselineStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryBaselineStatus): Boolean {
        when (status) {
            RedeliveryBaselineStatus.IDLE -> return next == RedeliveryBaselineStatus.STOPPED
            RedeliveryBaselineStatus.WARMING -> return next == RedeliveryBaselineStatus.DRAINING || next == RedeliveryBaselineStatus.STOPPED
            RedeliveryBaselineStatus.READY -> return next == RedeliveryBaselineStatus.DRAINING || next == RedeliveryBaselineStatus.STOPPED
            RedeliveryBaselineStatus.DRAINING -> return next == RedeliveryBaselineStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RedeliveryBaselineStatus) {
        if (!canMoveTo(next)) {
            error("redelivery baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedValue(): Double = round(weight * graceDays.toDouble() * 100) / 100

    fun snapshot(): RedeliveryBaseline {
        return RedeliveryBaseline(
            code = code,
            name = name,
            graceDays = graceDays,
            vehicleId = vehicleId,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a redelivery baseline in status idle. */
fun newRedeliveryBaseline(code: String, name: String): RedeliveryBaseline {
    return RedeliveryBaseline(
        code = code,
        name = name,
        graceDays = 0,
        vehicleId = "",
        weight = 0.0,
        status = RedeliveryBaselineStatus.IDLE,
    )
}
