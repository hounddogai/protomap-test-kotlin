package com.acme.transport.network.node.tier

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict node tier calculation. */
interface StrictNodeTierAdjuster {
    fun describe(): String
    fun adjust(item: StrictNodeTier, value: Double): Double
}

data class PercentStrictNodeTierAdjuster(var percent: Double) : StrictNodeTierAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictNodeTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumStrictNodeTierAdjuster(var minimum: Double) : StrictNodeTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictNodeTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleStrictNodeTierAdjuster(var factors: MutableMap<String, Double>) : StrictNodeTierAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictNodeTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingStrictNodeTierAdjuster(var step: Double) : StrictNodeTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictNodeTier, value: Double): Double = roundTo(value, step)
}

data class StrictNodeTierFloorValueOffsetAdjuster(var weight: Double) : StrictNodeTierAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: StrictNodeTier, value: Double): Double = value + item.floorValue * weight
}

/** Runs the strict node tier steps in order and keeps a trace of each. */
data class StrictNodeTierEngine(
    var steps: MutableList<StrictNodeTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictNodeTier): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictNodeTierAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictNodeTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictNodeTierEngine(): StrictNodeTierEngine {
    return StrictNodeTierEngine(
        steps = mutableListOf<StrictNodeTierAdjuster>(
            PercentStrictNodeTierAdjuster(-17.7),
            MinimumStrictNodeTierAdjuster(24.5),
            TitleStrictNodeTierAdjuster(mutableMapOf<String, Double>("retail" to 0.79)),
            RoundingStrictNodeTierAdjuster(0.5),
            StrictNodeTierFloorValueOffsetAdjuster(0.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 71584.9,
    )
}
