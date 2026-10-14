package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capacity plan calculation. */
interface CapacityPlanAdjuster {
    fun describe(): String
    fun adjust(item: CapacityPlan, value: Double): Double
}

data class CapCapacityPlanAdjuster(var cap: Double) : CapacityPlanAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityPlan, value: Double): Double = minOf(value, cap)
}

data class LabelCapacityPlanAdjuster(var factors: MutableMap<String, Double>) : CapacityPlanAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: CapacityPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCapacityPlanAdjuster(var step: Double) : CapacityPlanAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacityPlan, value: Double): Double = roundTo(value, step)
}

/** Runs the capacity plan steps in order and keeps a trace of each. */
data class CapacityPlanEngine(
    var steps: MutableList<CapacityPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CapacityPlan): Double {
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

    fun evaluateAll(items: MutableList<CapacityPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCapacityPlanEngine(): CapacityPlanEngine {
    return CapacityPlanEngine(
        steps = mutableListOf<CapacityPlanAdjuster>(
            CapCapacityPlanAdjuster(37186.0),
            LabelCapacityPlanAdjuster(mutableMapOf<String, Double>("west" to 1.07)),
            RoundingCapacityPlanAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 20740.8,
    )
}
