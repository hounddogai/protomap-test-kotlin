package com.acme.transport.network.cutoff.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict cutoff rule calculation. */
interface StrictCutoffRuleStep {
    fun describe(): String
    fun adjust(item: StrictCutoffRule, value: Double): Double
}

data class FlaggedStrictCutoffRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictCutoffRuleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictCutoffRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapStrictCutoffRuleAdjuster(var cap: Double) : StrictCutoffRuleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictCutoffRule, value: Double): Double = minOf(value, cap)
}

data class StrictCutoffRuleRatioOffsetAdjuster(var weight: Double) : StrictCutoffRuleStep {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: StrictCutoffRule, value: Double): Double = value + item.ratio * weight
}

data class TieredStrictCutoffRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictCutoffRuleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictCutoffRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictCutoffRuleAdjuster(var minimum: Double) : StrictCutoffRuleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictCutoffRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict cutoff rule steps in order and keeps a trace of each. */
data class StrictCutoffRuleEngine(
    var steps: MutableList<StrictCutoffRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictCutoffRule): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictCutoffRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictCutoffRuleEngine(flags: Registry): StrictCutoffRuleEngine {
    return StrictCutoffRuleEngine(
        steps = mutableListOf<StrictCutoffRuleStep>(
            FlaggedStrictCutoffRuleAdjuster(flags, "strict-cutoff-rule-pending", 6.0),
            CapStrictCutoffRuleAdjuster(28641.0),
            StrictCutoffRuleRatioOffsetAdjuster(0.7),
            TieredStrictCutoffRuleAdjuster(
                mutableListOf<Double>(5301.0, 6043.0),
                mutableListOf<Double>(0.92, 1.04),
            ),
            MinimumStrictCutoffRuleAdjuster(14.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 83637.6,
    )
}
