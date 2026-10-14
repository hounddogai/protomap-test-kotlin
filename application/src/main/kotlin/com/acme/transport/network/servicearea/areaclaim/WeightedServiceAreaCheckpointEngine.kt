package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted service area checkpoint calculation. */
interface WeightedServiceAreaCheckpointModifier {
    fun describe(): String
    fun adjust(item: WeightedServiceAreaCheckpoint, value: Double): Double
}

data class MinimumWeightedServiceAreaCheckpointAdjuster(
    var minimum: Double,
) : WeightedServiceAreaCheckpointModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedServiceAreaCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapWeightedServiceAreaCheckpointAdjuster(var cap: Double) : WeightedServiceAreaCheckpointModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedServiceAreaCheckpoint, value: Double): Double = minOf(value, cap)
}

data class RoundingWeightedServiceAreaCheckpointAdjuster(
    var step: Double,
) : WeightedServiceAreaCheckpointModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedServiceAreaCheckpoint, value: Double): Double = roundTo(value, step)
}

data class WeightedServiceAreaCheckpointDiscountOffsetAdjuster(
    var weight: Double,
) : WeightedServiceAreaCheckpointModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: WeightedServiceAreaCheckpoint, value: Double): Double = value + item.discount * weight
}

data class NameWeightedServiceAreaCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedServiceAreaCheckpointModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: WeightedServiceAreaCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentWeightedServiceAreaCheckpointAdjuster(
    var percent: Double,
) : WeightedServiceAreaCheckpointModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedServiceAreaCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted service area checkpoint steps in order and keeps a trace of each. */
data class WeightedServiceAreaCheckpointEngine(
    var steps: MutableList<WeightedServiceAreaCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedServiceAreaCheckpoint): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedServiceAreaCheckpointModifier = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedServiceAreaCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedServiceAreaCheckpointEngine(): WeightedServiceAreaCheckpointEngine {
    return WeightedServiceAreaCheckpointEngine(
        steps = mutableListOf<WeightedServiceAreaCheckpointModifier>(
            MinimumWeightedServiceAreaCheckpointAdjuster(25.5),
            CapWeightedServiceAreaCheckpointAdjuster(37925.0),
            RoundingWeightedServiceAreaCheckpointAdjuster(1.0),
            WeightedServiceAreaCheckpointDiscountOffsetAdjuster(1.16),
            NameWeightedServiceAreaCheckpointAdjuster(mutableMapOf<String, Double>("central" to 1.12)),
            PercentWeightedServiceAreaCheckpointAdjuster(2.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 6270.2,
    )
}
