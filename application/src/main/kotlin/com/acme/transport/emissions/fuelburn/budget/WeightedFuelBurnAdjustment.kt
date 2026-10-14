package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.kit.text.normalize

/** Where a weighted fuel burn adjustment is in its life cycle. */
enum class WeightedFuelBurnAdjustmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A weighted fuel burn adjustment as the fuel burn team keeps it. */
data class WeightedFuelBurnAdjustment(
    var code: String,
    var title: String,
    var summary: String,
    var channel: String,
    var pinned: Boolean,
    var currency: String,
    var startsAt: Int,
    var status: WeightedFuelBurnAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFuelBurnAdjustmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFuelBurnAdjustmentStatus): Boolean {
        when (status) {
            WeightedFuelBurnAdjustmentStatus.IDLE -> {
                when (next) {
                    WeightedFuelBurnAdjustmentStatus.WARMING, WeightedFuelBurnAdjustmentStatus.DRAINING -> return true
                }
                return false
            }
            WeightedFuelBurnAdjustmentStatus.WARMING -> return next == WeightedFuelBurnAdjustmentStatus.READY
            WeightedFuelBurnAdjustmentStatus.READY -> {
                when (next) {
                    WeightedFuelBurnAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
            WeightedFuelBurnAdjustmentStatus.DRAINING -> {
                when (next) {
                    WeightedFuelBurnAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedFuelBurnAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("weighted fuel burn adjustment $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"weighted fuel burn adjustment"} $code"

    fun measuredValue(): Double = startsAt.toDouble()

    fun snapshot(): WeightedFuelBurnAdjustment {
        return WeightedFuelBurnAdjustment(
            code = code,
            title = title,
            summary = summary,
            channel = channel,
            pinned = pinned,
            currency = currency,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted fuel burn adjustment in status idle. */
fun newWeightedFuelBurnAdjustment(code: String, title: String): WeightedFuelBurnAdjustment {
    return WeightedFuelBurnAdjustment(
        code = code,
        title = title,
        summary = "",
        channel = "",
        pinned = false,
        currency = "",
        startsAt = 0,
        status = WeightedFuelBurnAdjustmentStatus.IDLE,
    )
}
