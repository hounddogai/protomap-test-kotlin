package com.acme.transport.lastmile.drop.snapshot

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict drop tier calculation. */
interface StrictDropTierStep {
    fun describe(): String
    fun adjust(item: StrictDropTier, value: Double): Double
}

data class RoundingStrictDropTierAdjuster(var step: Double) : StrictDropTierStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictDropTier, value: Double): Double = roundTo(value, step)
}

data class MinimumStrictDropTierAdjuster(var minimum: Double) : StrictDropTierStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictDropTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredStrictDropTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictDropTierStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictDropTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class UnitStrictDropTierAdjuster(var factors: MutableMap<String, Double>) : StrictDropTierStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: StrictDropTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict drop tier steps in order and keeps a trace of each. */
data class StrictDropTierEngine(
    var steps: MutableList<StrictDropTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictDropTier): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictDropTierStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictDropTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictDropTierEngine(): StrictDropTierEngine {
    return StrictDropTierEngine(
        steps = mutableListOf<StrictDropTierStep>(
            RoundingStrictDropTierAdjuster(0.5),
            MinimumStrictDropTierAdjuster(6.6),
            TieredStrictDropTierAdjuster(
                mutableListOf<Double>(869.0, 3566.0, 4924.0),
                mutableListOf<Double>(1.14, 1.1, 0.88),
            ),
            UnitStrictDropTierAdjuster(mutableMapOf<String, Double>("north" to 1.3, "south" to 1.25)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 65579.0,
    )
}
