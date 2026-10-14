package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted inspection plan calculation. */
interface WeightedInspectionPlanFactor {
    fun describe(): String
    fun adjust(item: WeightedInspectionPlan, value: Double): Double
}

data class WeightedInspectionPlanCostOffsetAdjuster(var weight: Double) : WeightedInspectionPlanFactor {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: WeightedInspectionPlan, value: Double): Double = value + item.cost * weight
}

data class PercentWeightedInspectionPlanAdjuster(var percent: Double) : WeightedInspectionPlanFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedInspectionPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredWeightedInspectionPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedInspectionPlanFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedInspectionPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingWeightedInspectionPlanAdjuster(var step: Double) : WeightedInspectionPlanFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedInspectionPlan, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted inspection plan steps in order and keeps a trace of each. */
data class WeightedInspectionPlanEngine(
    var steps: MutableList<WeightedInspectionPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedInspectionPlan): Double {
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

    fun evaluateAll(items: MutableList<WeightedInspectionPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedInspectionPlanEngine(): WeightedInspectionPlanEngine {
    return WeightedInspectionPlanEngine(
        steps = mutableListOf<WeightedInspectionPlanFactor>(
            WeightedInspectionPlanCostOffsetAdjuster(1.63),
            PercentWeightedInspectionPlanAdjuster(5.6),
            TieredWeightedInspectionPlanAdjuster(
                mutableListOf<Double>(3547.0, 5336.0, 6291.0, 8219.0),
                mutableListOf<Double>(0.87, 1.06, 0.83, 0.91),
            ),
            RoundingWeightedInspectionPlanAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 91034.6,
    )
}
