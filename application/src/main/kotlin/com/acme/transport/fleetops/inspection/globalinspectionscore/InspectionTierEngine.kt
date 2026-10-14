package com.acme.transport.fleetops.inspection.globalinspectionscore

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection tier calculation. */
interface InspectionTierStep {
    fun describe(): String
    fun adjust(item: InspectionTier, value: Double): Double
}

data class MinimumInspectionTierAdjuster(var minimum: Double) : InspectionTierStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingInspectionTierAdjuster(var step: Double) : InspectionTierStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionTier, value: Double): Double = roundTo(value, step)
}

data class LabelInspectionTierAdjuster(var factors: MutableMap<String, Double>) : InspectionTierStep {
    override fun describe(): String = "by label"

    override fun adjust(item: InspectionTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the inspection tier steps in order and keeps a trace of each. */
data class InspectionTierEngine(
    var steps: MutableList<InspectionTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionTier): Double {
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

    fun evaluateAll(items: MutableList<InspectionTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionTierEngine(): InspectionTierEngine {
    return InspectionTierEngine(
        steps = mutableListOf<InspectionTierStep>(
            MinimumInspectionTierAdjuster(6.8),
            RoundingInspectionTierAdjuster(0.05),
            LabelInspectionTierAdjuster(mutableMapOf<String, Double>("west" to 0.79, "retail" to 0.93)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 49431.0,
    )
}
