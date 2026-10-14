package com.acme.transport.lastmile.drop.dropnotice

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict drop policy calculation. */
interface StrictDropPolicyStep {
    fun describe(): String
    fun adjust(item: StrictDropPolicy, value: Double): Double
}

data class TieredStrictDropPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictDropPolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictDropPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictDropPolicyAdjuster(var minimum: Double) : StrictDropPolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictDropPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingStrictDropPolicyAdjuster(var step: Double) : StrictDropPolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictDropPolicy, value: Double): Double = roundTo(value, step)
}

data class CountryStrictDropPolicyAdjuster(var factors: MutableMap<String, Double>) : StrictDropPolicyStep {
    override fun describe(): String = "by country"

    override fun adjust(item: StrictDropPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict drop policy steps in order and keeps a trace of each. */
data class StrictDropPolicyEngine(
    var steps: MutableList<StrictDropPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictDropPolicy): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictDropPolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictDropPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictDropPolicyEngine(): StrictDropPolicyEngine {
    return StrictDropPolicyEngine(
        steps = mutableListOf<StrictDropPolicyStep>(
            TieredStrictDropPolicyAdjuster(
                mutableListOf<Double>(459.0, 1927.0),
                mutableListOf<Double>(1.19, 0.81),
            ),
            MinimumStrictDropPolicyAdjuster(24.4),
            RoundingStrictDropPolicyAdjuster(0.1),
            CountryStrictDropPolicyAdjuster(
                mutableMapOf<String, Double>("central" to 0.79, "north" to 0.72, "south" to 1.25),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 90234.0,
    )
}
