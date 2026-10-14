package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted cutoff entry calculation. */
interface WeightedCutoffEntryStep {
    fun describe(): String
    fun adjust(item: WeightedCutoffEntry, value: Double): Double
}

data class CapWeightedCutoffEntryAdjuster(var cap: Double) : WeightedCutoffEntryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedCutoffEntry, value: Double): Double = minOf(value, cap)
}

data class PercentWeightedCutoffEntryAdjuster(var percent: Double) : WeightedCutoffEntryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedCutoffEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredWeightedCutoffEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedCutoffEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedCutoffEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelWeightedCutoffEntryAdjuster(var factors: MutableMap<String, Double>) : WeightedCutoffEntryStep {
    override fun describe(): String = "by label"

    override fun adjust(item: WeightedCutoffEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumWeightedCutoffEntryAdjuster(var minimum: Double) : WeightedCutoffEntryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedCutoffEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted cutoff entry steps in order and keeps a trace of each. */
data class WeightedCutoffEntryEngine(
    var steps: MutableList<WeightedCutoffEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedCutoffEntry): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedCutoffEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedCutoffEntryEngine(): WeightedCutoffEntryEngine {
    return WeightedCutoffEntryEngine(
        steps = mutableListOf<WeightedCutoffEntryStep>(
            CapWeightedCutoffEntryAdjuster(44141.0),
            PercentWeightedCutoffEntryAdjuster(-16.1),
            TieredWeightedCutoffEntryAdjuster(
                mutableListOf<Double>(609.0, 5649.0, 8923.0),
                mutableListOf<Double>(1.19, 1.06, 0.83),
            ),
            LabelWeightedCutoffEntryAdjuster(mutableMapOf<String, Double>("east" to 1.03)),
            MinimumWeightedCutoffEntryAdjuster(4.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 51761.7,
    )
}
