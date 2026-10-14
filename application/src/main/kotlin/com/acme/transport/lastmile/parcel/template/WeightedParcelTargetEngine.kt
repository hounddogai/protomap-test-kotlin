package com.acme.transport.lastmile.parcel.template

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted parcel target calculation. */
interface WeightedParcelTargetFactor {
    fun describe(): String
    fun adjust(item: WeightedParcelTarget, value: Double): Double
}

data class MinimumWeightedParcelTargetAdjuster(var minimum: Double) : WeightedParcelTargetFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedParcelTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentWeightedParcelTargetAdjuster(var percent: Double) : WeightedParcelTargetFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedParcelTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapWeightedParcelTargetAdjuster(var cap: Double) : WeightedParcelTargetFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedParcelTarget, value: Double): Double = minOf(value, cap)
}

data class RegionWeightedParcelTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedParcelTargetFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: WeightedParcelTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted parcel target steps in order and keeps a trace of each. */
data class WeightedParcelTargetEngine(
    var steps: MutableList<WeightedParcelTargetFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedParcelTarget): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedParcelTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedParcelTargetEngine(): WeightedParcelTargetEngine {
    return WeightedParcelTargetEngine(
        steps = mutableListOf<WeightedParcelTargetFactor>(
            MinimumWeightedParcelTargetAdjuster(16.0),
            PercentWeightedParcelTargetAdjuster(23.8),
            CapWeightedParcelTargetAdjuster(3310.0),
            RegionWeightedParcelTargetAdjuster(
                mutableMapOf<String, Double>("online" to 0.98, "north" to 1.04, "central" to 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 23074.0,
    )
}
