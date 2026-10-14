package com.acme.transport.emissions.fuelburn.burnnotice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn metric is in its life cycle. */
enum class FuelBurnMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a fuel burn metric and its current status. */
data class FuelBurnMetric(
    var code: String,
    var name: String,
    var ceiling: Double,
    var lane: String,
    var threshold: Double,
    var maximum: Double,
    var limit: Int,
    var timezone: String,
    var pinned: Boolean,
    var minimum: Double,
    var status: FuelBurnMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnMetricStatus): Boolean {
        when (status) {
            FuelBurnMetricStatus.IDLE -> {
                when (next) {
                    FuelBurnMetricStatus.READY, FuelBurnMetricStatus.STOPPED -> return true
                }
                return false
            }
            FuelBurnMetricStatus.WARMING -> return next == FuelBurnMetricStatus.STOPPED
            FuelBurnMetricStatus.READY -> return next == FuelBurnMetricStatus.DRAINING || next == FuelBurnMetricStatus.STOPPED
            FuelBurnMetricStatus.DRAINING -> {
                when (next) {
                    FuelBurnMetricStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnMetricStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedLoad(): Double = round((limit.toDouble() - maximum) * 100) / 100

    fun snapshot(): FuelBurnMetric {
        return FuelBurnMetric(
            code = code,
            name = name,
            ceiling = ceiling,
            lane = lane,
            threshold = threshold,
            maximum = maximum,
            limit = limit,
            timezone = timezone,
            pinned = pinned,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fuel burn metric in status idle. */
fun newFuelBurnMetric(code: String, name: String): FuelBurnMetric {
    return FuelBurnMetric(
        code = code,
        name = name,
        ceiling = 0.0,
        lane = "",
        threshold = 0.0,
        maximum = 0.0,
        limit = 0,
        timezone = "",
        pinned = false,
        minimum = 0.0,
        status = FuelBurnMetricStatus.IDLE,
    )
}
