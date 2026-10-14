package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending availability tier calculation. */
interface PendingAvailabilityTierAdjuster {
    fun describe(): String
    fun adjust(item: PendingAvailabilityTier, value: Double): Double
}

data class PendingAvailabilityTierRateOffsetAdjuster(var weight: Double) : PendingAvailabilityTierAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: PendingAvailabilityTier, value: Double): Double = value + item.rate * weight
}

data class MinimumPendingAvailabilityTierAdjuster(var minimum: Double) : PendingAvailabilityTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingAvailabilityTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPendingAvailabilityTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingAvailabilityTierAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingAvailabilityTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedPendingAvailabilityTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingAvailabilityTierAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingAvailabilityTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPendingAvailabilityTierAdjuster(var cap: Double) : PendingAvailabilityTierAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingAvailabilityTier, value: Double): Double = minOf(value, cap)
}

/** Runs the pending availability tier steps in order and keeps a trace of each. */
data class PendingAvailabilityTierEngine(
    var steps: MutableList<PendingAvailabilityTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingAvailabilityTier): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingAvailabilityTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingAvailabilityTierEngine(flags: Registry): PendingAvailabilityTierEngine {
    return PendingAvailabilityTierEngine(
        steps = mutableListOf<PendingAvailabilityTierAdjuster>(
            PendingAvailabilityTierRateOffsetAdjuster(1.74),
            MinimumPendingAvailabilityTierAdjuster(11.3),
            TieredPendingAvailabilityTierAdjuster(
                mutableListOf<Double>(244.0, 4227.0, 6842.0),
                mutableListOf<Double>(0.89, 0.84, 0.91),
            ),
            FlaggedPendingAvailabilityTierAdjuster(flags, "pending-availability-tier-pending", -9.1),
            CapPendingAvailabilityTierAdjuster(29569.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 31891.9,
    )
}
