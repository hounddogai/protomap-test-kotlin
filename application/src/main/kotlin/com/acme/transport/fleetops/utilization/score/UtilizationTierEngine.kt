package com.acme.transport.fleetops.utilization.score

import com.acme.transport.kit.flags.Registry

/** One step of the utilization tier calculation. */
interface UtilizationTierAdjuster {
    fun describe(): String
    fun adjust(item: UtilizationTier, value: Double): Double
}

data class LabelUtilizationTierAdjuster(var factors: MutableMap<String, Double>) : UtilizationTierAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: UtilizationTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class UtilizationTierRateOffsetAdjuster(var weight: Double) : UtilizationTierAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: UtilizationTier, value: Double): Double = value + item.rate * weight
}

data class CapUtilizationTierAdjuster(var cap: Double) : UtilizationTierAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: UtilizationTier, value: Double): Double = minOf(value, cap)
}

data class MinimumUtilizationTierAdjuster(var minimum: Double) : UtilizationTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedUtilizationTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationTierAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentUtilizationTierAdjuster(var percent: Double) : UtilizationTierAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the utilization tier steps in order and keeps a trace of each. */
data class UtilizationTierEngine(
    var steps: MutableList<UtilizationTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: UtilizationTier): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: UtilizationTierAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<UtilizationTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultUtilizationTierEngine(flags: Registry): UtilizationTierEngine {
    return UtilizationTierEngine(
        steps = mutableListOf<UtilizationTierAdjuster>(
            LabelUtilizationTierAdjuster(mutableMapOf<String, Double>("west" to 1.14, "online" to 0.78)),
            UtilizationTierRateOffsetAdjuster(1.02),
            CapUtilizationTierAdjuster(12472.0),
            MinimumUtilizationTierAdjuster(0.6),
            FlaggedUtilizationTierAdjuster(flags, "utilization-tier-rolling", -5.3),
            PercentUtilizationTierAdjuster(6.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 11200.2,
    )
}
