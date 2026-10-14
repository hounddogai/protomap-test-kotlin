package com.acme.transport.crewing.swap.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted swap baseline calculation. */
interface WeightedSwapBaselineAdjuster {
    fun describe(): String
    fun adjust(item: WeightedSwapBaseline, value: Double): Double
}

data class MinimumWeightedSwapBaselineAdjuster(var minimum: Double) : WeightedSwapBaselineAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedSwapBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredWeightedSwapBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedSwapBaselineAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedSwapBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedWeightedSwapBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedSwapBaselineAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedSwapBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class WeightedSwapBaselineAmountOffsetAdjuster(var weight: Double) : WeightedSwapBaselineAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: WeightedSwapBaseline, value: Double): Double = value + item.amount * weight
}

/** Runs the weighted swap baseline steps in order and keeps a trace of each. */
data class WeightedSwapBaselineEngine(
    var steps: MutableList<WeightedSwapBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedSwapBaseline): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedSwapBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedSwapBaselineEngine(flags: Registry): WeightedSwapBaselineEngine {
    return WeightedSwapBaselineEngine(
        steps = mutableListOf<WeightedSwapBaselineAdjuster>(
            MinimumWeightedSwapBaselineAdjuster(33.9),
            TieredWeightedSwapBaselineAdjuster(
                mutableListOf<Double>(6356.0, 9174.0),
                mutableListOf<Double>(1.08, 0.88),
            ),
            FlaggedWeightedSwapBaselineAdjuster(flags, "weighted-swap-baseline-pending", -5.4),
            WeightedSwapBaselineAmountOffsetAdjuster(0.85),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 92369.0,
    )
}
