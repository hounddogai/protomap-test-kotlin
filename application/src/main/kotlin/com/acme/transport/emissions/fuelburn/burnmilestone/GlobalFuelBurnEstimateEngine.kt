package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global fuel burn estimate calculation. */
interface GlobalFuelBurnEstimateAdjuster {
    fun describe(): String
    fun adjust(item: GlobalFuelBurnEstimate, value: Double): Double
}

data class GlobalFuelBurnEstimateLimitOffsetAdjuster(var weight: Double) : GlobalFuelBurnEstimateAdjuster {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: GlobalFuelBurnEstimate, value: Double): Double = value + item.limit.toDouble() * weight
}

data class TieredGlobalFuelBurnEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalFuelBurnEstimateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalFuelBurnEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedGlobalFuelBurnEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalFuelBurnEstimateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalFuelBurnEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalFuelBurnEstimateAdjuster(var step: Double) : GlobalFuelBurnEstimateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalFuelBurnEstimate, value: Double): Double = roundTo(value, step)
}

data class MinimumGlobalFuelBurnEstimateAdjuster(var minimum: Double) : GlobalFuelBurnEstimateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalFuelBurnEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapGlobalFuelBurnEstimateAdjuster(var cap: Double) : GlobalFuelBurnEstimateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalFuelBurnEstimate, value: Double): Double = minOf(value, cap)
}

/** Runs the global fuel burn estimate steps in order and keeps a trace of each. */
data class GlobalFuelBurnEstimateEngine(
    var steps: MutableList<GlobalFuelBurnEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalFuelBurnEstimate): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalFuelBurnEstimateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalFuelBurnEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalFuelBurnEstimateEngine(flags: Registry): GlobalFuelBurnEstimateEngine {
    return GlobalFuelBurnEstimateEngine(
        steps = mutableListOf<GlobalFuelBurnEstimateAdjuster>(
            GlobalFuelBurnEstimateLimitOffsetAdjuster(0.89),
            TieredGlobalFuelBurnEstimateAdjuster(
                mutableListOf<Double>(289.0, 7535.0),
                mutableListOf<Double>(1.09, 1.13),
            ),
            FlaggedGlobalFuelBurnEstimateAdjuster(flags, "global-fuel-burn-estimate-weighted", 5.8),
            RoundingGlobalFuelBurnEstimateAdjuster(0.1),
            MinimumGlobalFuelBurnEstimateAdjuster(42.5),
            CapGlobalFuelBurnEstimateAdjuster(18058.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 45472.6,
    )
}
