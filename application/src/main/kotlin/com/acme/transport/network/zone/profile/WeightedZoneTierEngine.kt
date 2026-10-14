package com.acme.transport.network.zone.profile

import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted zone tier calculation. */
interface WeightedZoneTierAdjuster {
    fun describe(): String
    fun adjust(item: WeightedZoneTier, value: Double): Double
}

data class WeightedZoneTierCapacityOffsetAdjuster(var weight: Double) : WeightedZoneTierAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: WeightedZoneTier, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class RoundingWeightedZoneTierAdjuster(var step: Double) : WeightedZoneTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedZoneTier, value: Double): Double = roundTo(value, step)
}

data class LabelWeightedZoneTierAdjuster(var factors: MutableMap<String, Double>) : WeightedZoneTierAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: WeightedZoneTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumWeightedZoneTierAdjuster(var minimum: Double) : WeightedZoneTierAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedZoneTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapWeightedZoneTierAdjuster(var cap: Double) : WeightedZoneTierAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedZoneTier, value: Double): Double = minOf(value, cap)
}

data class TieredWeightedZoneTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedZoneTierAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedZoneTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted zone tier steps in order and keeps a trace of each. */
data class WeightedZoneTierEngine(
    var steps: MutableList<WeightedZoneTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedZoneTier): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedZoneTierAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedZoneTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedZoneTierEngine(): WeightedZoneTierEngine {
    return WeightedZoneTierEngine(
        steps = mutableListOf<WeightedZoneTierAdjuster>(
            WeightedZoneTierCapacityOffsetAdjuster(1.59),
            RoundingWeightedZoneTierAdjuster(0.05),
            LabelWeightedZoneTierAdjuster(mutableMapOf<String, Double>("central" to 1.39)),
            MinimumWeightedZoneTierAdjuster(32.8),
            CapWeightedZoneTierAdjuster(16715.0),
            TieredWeightedZoneTierAdjuster(
                mutableListOf<Double>(4040.0, 5047.0, 5587.0, 7074.0),
                mutableListOf<Double>(1.1, 0.91, 1.11, 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 45116.9,
    )
}
