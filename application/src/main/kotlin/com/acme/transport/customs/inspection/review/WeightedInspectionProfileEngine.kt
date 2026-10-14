package com.acme.transport.customs.inspection.review

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted inspection profile calculation. */
interface WeightedInspectionProfileModifier {
    fun describe(): String
    fun adjust(item: WeightedInspectionProfile, value: Double): Double
}

data class TieredWeightedInspectionProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedInspectionProfileModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedInspectionProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingWeightedInspectionProfileAdjuster(var step: Double) : WeightedInspectionProfileModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedInspectionProfile, value: Double): Double = roundTo(value, step)
}

data class MinimumWeightedInspectionProfileAdjuster(var minimum: Double) : WeightedInspectionProfileModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedInspectionProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted inspection profile steps in order and keeps a trace of each. */
data class WeightedInspectionProfileEngine(
    var steps: MutableList<WeightedInspectionProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedInspectionProfile): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedInspectionProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedInspectionProfileEngine(): WeightedInspectionProfileEngine {
    return WeightedInspectionProfileEngine(
        steps = mutableListOf<WeightedInspectionProfileModifier>(
            TieredWeightedInspectionProfileAdjuster(
                mutableListOf<Double>(1952.0, 6989.0),
                mutableListOf<Double>(0.99, 0.98),
            ),
            RoundingWeightedInspectionProfileAdjuster(1.0),
            MinimumWeightedInspectionProfileAdjuster(15.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 28965.9,
    )
}
