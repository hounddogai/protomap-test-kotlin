package com.acme.transport.network.cutoff.cutoffledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global cutoff rollup calculation. */
interface GlobalCutoffRollupFactor {
    fun describe(): String
    fun adjust(item: GlobalCutoffRollup, value: Double): Double
}

data class GlobalCutoffRollupQuantityOffsetAdjuster(var weight: Double) : GlobalCutoffRollupFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: GlobalCutoffRollup, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class FlaggedGlobalCutoffRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalCutoffRollupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalCutoffRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalCutoffRollupAdjuster(var step: Double) : GlobalCutoffRollupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalCutoffRollup, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalCutoffRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalCutoffRollupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalCutoffRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumGlobalCutoffRollupAdjuster(var minimum: Double) : GlobalCutoffRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalCutoffRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global cutoff rollup steps in order and keeps a trace of each. */
data class GlobalCutoffRollupEngine(
    var steps: MutableList<GlobalCutoffRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalCutoffRollup): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalCutoffRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalCutoffRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalCutoffRollupEngine(flags: Registry): GlobalCutoffRollupEngine {
    return GlobalCutoffRollupEngine(
        steps = mutableListOf<GlobalCutoffRollupFactor>(
            GlobalCutoffRollupQuantityOffsetAdjuster(1.51),
            FlaggedGlobalCutoffRollupAdjuster(flags, "global-cutoff-rollup-rolling", 9.9),
            RoundingGlobalCutoffRollupAdjuster(5.0),
            TieredGlobalCutoffRollupAdjuster(
                mutableListOf<Double>(4800.0, 5398.0, 8943.0),
                mutableListOf<Double>(1.17, 0.95, 1.03),
            ),
            MinimumGlobalCutoffRollupAdjuster(42.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 7587.6,
    )
}
