package com.acme.transport.linehaul.backhaul.backhaulvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict backhaul variance is in its life cycle. */
enum class StrictBackhaulVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A strict backhaul variance as the backhaul team keeps it. */
data class StrictBackhaulVariance(
    var code: String,
    var name: String,
    var lane: String,
    var category: String,
    var ceiling: Double,
    var currency: String,
    var loadKg: Double,
    var trailerId: String,
    var status: StrictBackhaulVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictBackhaulVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictBackhaulVarianceStatus): Boolean {
        when (status) {
            StrictBackhaulVarianceStatus.IDLE -> {
                when (next) {
                    StrictBackhaulVarianceStatus.READY -> return true
                }
                return false
            }
            StrictBackhaulVarianceStatus.WARMING -> {
                when (next) {
                    StrictBackhaulVarianceStatus.READY, StrictBackhaulVarianceStatus.STOPPED -> return true
                }
                return false
            }
            StrictBackhaulVarianceStatus.READY -> return next == StrictBackhaulVarianceStatus.DRAINING
            StrictBackhaulVarianceStatus.DRAINING -> return next == StrictBackhaulVarianceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: StrictBackhaulVarianceStatus) {
        if (!canMoveTo(next)) {
            error("strict backhaul variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict backhaul variance"} $code"

    fun effectiveLoad(): Double = round((loadKg + ceiling) * 100) / 100

    fun snapshot(): StrictBackhaulVariance {
        return StrictBackhaulVariance(
            code = code,
            name = name,
            lane = lane,
            category = category,
            ceiling = ceiling,
            currency = currency,
            loadKg = loadKg,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict backhaul variance in status idle. */
fun newStrictBackhaulVariance(code: String, name: String): StrictBackhaulVariance {
    return StrictBackhaulVariance(
        code = code,
        name = name,
        lane = "",
        category = "",
        ceiling = 0.0,
        currency = "",
        loadKg = 0.0,
        trailerId = "",
        status = StrictBackhaulVarianceStatus.IDLE,
    )
}
