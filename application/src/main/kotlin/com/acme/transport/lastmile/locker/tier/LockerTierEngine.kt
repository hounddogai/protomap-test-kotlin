package com.acme.transport.lastmile.locker.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the locker tier calculation. */
interface LockerTierFactor {
    fun describe(): String
    fun adjust(item: LockerTier, value: Double): Double
}

data class PercentLockerTierAdjuster(var percent: Double) : LockerTierFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LockerTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLockerTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LockerTierFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LockerTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLockerTierAdjuster(var step: Double) : LockerTierFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LockerTier, value: Double): Double = roundTo(value, step)
}

data class TieredLockerTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerTierFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLockerTierAdjuster(var minimum: Double) : LockerTierFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LockerTierLoadKgOffsetAdjuster(var weight: Double) : LockerTierFactor {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: LockerTier, value: Double): Double = value + item.loadKg * weight
}

/** Runs the locker tier steps in order and keeps a trace of each. */
data class LockerTierEngine(
    var steps: MutableList<LockerTierFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LockerTier): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerTierFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LockerTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLockerTierEngine(flags: Registry): LockerTierEngine {
    return LockerTierEngine(
        steps = mutableListOf<LockerTierFactor>(
            PercentLockerTierAdjuster(20.4),
            FlaggedLockerTierAdjuster(flags, "locker-tier-default", 0.0),
            RoundingLockerTierAdjuster(0.05),
            TieredLockerTierAdjuster(
                mutableListOf<Double>(3274.0, 3555.0, 5232.0),
                mutableListOf<Double>(0.96, 0.86, 1.13),
            ),
            MinimumLockerTierAdjuster(47.5),
            LockerTierLoadKgOffsetAdjuster(1.04),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 47707.7,
    )
}
