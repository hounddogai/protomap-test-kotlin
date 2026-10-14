package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted rest break override calculation. */
interface WeightedRestBreakOverrideModifier {
    fun describe(): String
    fun adjust(item: WeightedRestBreakOverride, value: Double): Double
}

data class MinimumWeightedRestBreakOverrideAdjuster(var minimum: Double) : WeightedRestBreakOverrideModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedRestBreakOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentWeightedRestBreakOverrideAdjuster(var percent: Double) : WeightedRestBreakOverrideModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedRestBreakOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapWeightedRestBreakOverrideAdjuster(var cap: Double) : WeightedRestBreakOverrideModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedRestBreakOverride, value: Double): Double = minOf(value, cap)
}

data class WeightedRestBreakOverrideRateOffsetAdjuster(var weight: Double) : WeightedRestBreakOverrideModifier {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: WeightedRestBreakOverride, value: Double): Double = value + item.rate * weight
}

/** Runs the weighted rest break override steps in order and keeps a trace of each. */
data class WeightedRestBreakOverrideEngine(
    var steps: MutableList<WeightedRestBreakOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedRestBreakOverride): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedRestBreakOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedRestBreakOverrideEngine(): WeightedRestBreakOverrideEngine {
    return WeightedRestBreakOverrideEngine(
        steps = mutableListOf<WeightedRestBreakOverrideModifier>(
            MinimumWeightedRestBreakOverrideAdjuster(44.5),
            PercentWeightedRestBreakOverrideAdjuster(8.1),
            CapWeightedRestBreakOverrideAdjuster(17843.0),
            WeightedRestBreakOverrideRateOffsetAdjuster(1.39),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 33271.3,
    )
}
