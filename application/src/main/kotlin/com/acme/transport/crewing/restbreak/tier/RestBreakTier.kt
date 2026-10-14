package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rest break tier records of crewing's rest break work, and the rules, stores, and calculations around
// them.

/** Where a rest break tier is in its life cycle. */
enum class RestBreakTierStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A rest break tier as the rest break team keeps it. */
data class RestBreakTier(
    var code: String,
    var name: String,
    var vehicleId: String,
    var channel: String,
    var score: Double,
    var cost: Double,
    var status: RestBreakTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakTierStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakTierStatus): Boolean {
        when (status) {
            RestBreakTierStatus.IDLE -> {
                when (next) {
                    RestBreakTierStatus.READY, RestBreakTierStatus.STOPPED -> return true
                }
                return false
            }
            RestBreakTierStatus.WARMING -> return next == RestBreakTierStatus.DRAINING || next == RestBreakTierStatus.STOPPED
            RestBreakTierStatus.READY -> return next == RestBreakTierStatus.DRAINING || next == RestBreakTierStatus.STOPPED
            RestBreakTierStatus.DRAINING -> {
                when (next) {
                    RestBreakTierStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RestBreakTierStatus) {
        if (!canMoveTo(next)) {
            error("rest break tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedTotal(): Double = round((score + cost) * 100) / 100

    fun snapshot(): RestBreakTier {
        return RestBreakTier(
            code = code,
            name = name,
            vehicleId = vehicleId,
            channel = channel,
            score = score,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a rest break tier in status idle. */
fun newRestBreakTier(code: String, name: String): RestBreakTier {
    return RestBreakTier(
        code = code,
        name = name,
        vehicleId = "",
        channel = "",
        score = 0.0,
        cost = 0.0,
        status = RestBreakTierStatus.IDLE,
    )
}
