package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted inspection baseline calculation. */
interface WeightedInspectionBaselineFactor {
    fun describe(): String
    fun adjust(item: WeightedInspectionBaseline, value: Double): Double
}

data class LocaleWeightedInspectionBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedInspectionBaselineFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: WeightedInspectionBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapWeightedInspectionBaselineAdjuster(var cap: Double) : WeightedInspectionBaselineFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedInspectionBaseline, value: Double): Double = minOf(value, cap)
}

data class WeightedInspectionBaselineLoadKgOffsetAdjuster(
    var weight: Double,
) : WeightedInspectionBaselineFactor {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: WeightedInspectionBaseline, value: Double): Double = value + item.loadKg * weight
}

/** Runs the weighted inspection baseline steps in order and keeps a trace of each. */
data class WeightedInspectionBaselineEngine(
    var steps: MutableList<WeightedInspectionBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedInspectionBaseline): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedInspectionBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedInspectionBaselineEngine(): WeightedInspectionBaselineEngine {
    return WeightedInspectionBaselineEngine(
        steps = mutableListOf<WeightedInspectionBaselineFactor>(
            LocaleWeightedInspectionBaselineAdjuster(
                mutableMapOf<String, Double>("south" to 1.06, "central" to 1.35),
            ),
            CapWeightedInspectionBaselineAdjuster(10988.0),
            WeightedInspectionBaselineLoadKgOffsetAdjuster(0.98),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 67456.1,
    )
}
