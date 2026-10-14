package com.acme.transport.customs.inspection.batch

import com.acme.transport.kit.numbers.roundTo

/** One step of the inspection bundle calculation. */
interface InspectionBundleAdjuster {
    fun describe(): String
    fun adjust(item: InspectionBundle, value: Double): Double
}

data class PercentInspectionBundleAdjuster(var percent: Double) : InspectionBundleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapInspectionBundleAdjuster(var cap: Double) : InspectionBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionBundle, value: Double): Double = minOf(value, cap)
}

data class MinimumInspectionBundleAdjuster(var minimum: Double) : InspectionBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CategoryInspectionBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : InspectionBundleAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: InspectionBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingInspectionBundleAdjuster(var step: Double) : InspectionBundleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionBundle, value: Double): Double = roundTo(value, step)
}

data class InspectionBundleWindowDaysOffsetAdjuster(var weight: Double) : InspectionBundleAdjuster {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: InspectionBundle, value: Double): Double = value + item.windowDays.toDouble() * weight
}

/** Runs the inspection bundle steps in order and keeps a trace of each. */
data class InspectionBundleEngine(
    var steps: MutableList<InspectionBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: InspectionBundle): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionBundleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultInspectionBundleEngine(): InspectionBundleEngine {
    return InspectionBundleEngine(
        steps = mutableListOf<InspectionBundleAdjuster>(
            PercentInspectionBundleAdjuster(2.3),
            CapInspectionBundleAdjuster(28202.0),
            MinimumInspectionBundleAdjuster(19.0),
            CategoryInspectionBundleAdjuster(mutableMapOf<String, Double>("east" to 0.97, "south" to 0.89)),
            RoundingInspectionBundleAdjuster(0.25),
            InspectionBundleWindowDaysOffsetAdjuster(1.54),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 67041.9,
    )
}
