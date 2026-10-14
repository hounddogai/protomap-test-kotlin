package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the inspection threshold calculation. */
interface InspectionThresholdModifier {
    fun describe(): String
    fun adjust(item: InspectionThreshold, value: Double): Double
}

data class NameInspectionThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : InspectionThresholdModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: InspectionThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingInspectionThresholdAdjuster(var step: Double) : InspectionThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionThreshold, value: Double): Double = roundTo(value, step)
}

data class CapInspectionThresholdAdjuster(var cap: Double) : InspectionThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionThreshold, value: Double): Double = minOf(value, cap)
}

/** Runs the inspection threshold steps in order and keeps a trace of each. */
data class InspectionThresholdEngine(
    var steps: MutableList<InspectionThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: InspectionThreshold): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionThresholdModifier = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultInspectionThresholdEngine(): InspectionThresholdEngine {
    return InspectionThresholdEngine(
        steps = mutableListOf<InspectionThresholdModifier>(
            NameInspectionThresholdAdjuster(mutableMapOf<String, Double>("retail" to 1.38, "east" to 1.14)),
            RoundingInspectionThresholdAdjuster(1.0),
            CapInspectionThresholdAdjuster(17014.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 14121.3,
    )
}
