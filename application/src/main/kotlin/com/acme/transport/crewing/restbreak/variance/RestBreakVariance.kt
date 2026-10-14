package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rest break variance records of crewing's rest break work, and the rules, stores, and calculations
// around them.

/** Where a rest break variance is in its life cycle. */
enum class RestBreakVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a rest break variance and its current status. */
data class RestBreakVariance(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var lane: String,
    var discount: Double,
    var cost: Double,
    var endsAt: Int,
    var amount: Double,
    var channel: String,
    var fuelLiters: Double,
    var region: String,
    var status: RestBreakVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RestBreakVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RestBreakVarianceStatus): Boolean {
        when (status) {
            RestBreakVarianceStatus.IDLE -> return next == RestBreakVarianceStatus.READY
            RestBreakVarianceStatus.WARMING -> {
                when (next) {
                    RestBreakVarianceStatus.READY -> return true
                }
                return false
            }
            RestBreakVarianceStatus.READY -> {
                when (next) {
                    RestBreakVarianceStatus.DRAINING, RestBreakVarianceStatus.STOPPED -> return true
                }
                return false
            }
            RestBreakVarianceStatus.DRAINING -> return next == RestBreakVarianceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RestBreakVarianceStatus) {
        if (!canMoveTo(next)) {
            error("rest break variance $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveVolume(): Double = round((amount + endsAt.toDouble()) * 100) / 100

    fun snapshot(): RestBreakVariance {
        return RestBreakVariance(
            code = code,
            name = name,
            distanceKm = distanceKm,
            lane = lane,
            discount = discount,
            cost = cost,
            endsAt = endsAt,
            amount = amount,
            channel = channel,
            fuelLiters = fuelLiters,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rest break variance in status idle. */
fun newRestBreakVariance(code: String, name: String): RestBreakVariance {
    return RestBreakVariance(
        code = code,
        name = name,
        distanceKm = 0.0,
        lane = "",
        discount = 0.0,
        cost = 0.0,
        endsAt = 0,
        amount = 0.0,
        channel = "",
        fuelLiters = 0.0,
        region = "",
        status = RestBreakVarianceStatus.IDLE,
    )
}
