package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted availability group calculation. */
interface WeightedAvailabilityGroupStep {
    fun describe(): String
    fun adjust(item: WeightedAvailabilityGroup, value: Double): Double
}

data class CapWeightedAvailabilityGroupAdjuster(var cap: Double) : WeightedAvailabilityGroupStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedAvailabilityGroup, value: Double): Double = minOf(value, cap)
}

data class MinimumWeightedAvailabilityGroupAdjuster(var minimum: Double) : WeightedAvailabilityGroupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedAvailabilityGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentWeightedAvailabilityGroupAdjuster(var percent: Double) : WeightedAvailabilityGroupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedAvailabilityGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredWeightedAvailabilityGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedAvailabilityGroupStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedAvailabilityGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameWeightedAvailabilityGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedAvailabilityGroupStep {
    override fun describe(): String = "by name"

    override fun adjust(item: WeightedAvailabilityGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted availability group steps in order and keeps a trace of each. */
data class WeightedAvailabilityGroupEngine(
    var steps: MutableList<WeightedAvailabilityGroupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedAvailabilityGroup): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedAvailabilityGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedAvailabilityGroupEngine(): WeightedAvailabilityGroupEngine {
    return WeightedAvailabilityGroupEngine(
        steps = mutableListOf<WeightedAvailabilityGroupStep>(
            CapWeightedAvailabilityGroupAdjuster(10484.0),
            MinimumWeightedAvailabilityGroupAdjuster(46.7),
            PercentWeightedAvailabilityGroupAdjuster(4.1),
            TieredWeightedAvailabilityGroupAdjuster(
                mutableListOf<Double>(4982.0, 9802.0),
                mutableListOf<Double>(1.01, 0.82),
            ),
            NameWeightedAvailabilityGroupAdjuster(
                mutableMapOf<String, Double>("online" to 0.83, "west" to 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 28359.1,
    )
}
