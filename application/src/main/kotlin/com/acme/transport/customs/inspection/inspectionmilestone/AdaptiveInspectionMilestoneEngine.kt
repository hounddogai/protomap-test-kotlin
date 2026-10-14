package com.acme.transport.customs.inspection.inspectionmilestone

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive inspection milestone calculation. */
interface AdaptiveInspectionMilestoneFactor {
    fun describe(): String
    fun adjust(item: AdaptiveInspectionMilestone, value: Double): Double
}

data class AdaptiveInspectionMilestoneMaximumOffsetAdjuster(
    var weight: Double,
) : AdaptiveInspectionMilestoneFactor {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: AdaptiveInspectionMilestone, value: Double): Double = value + item.maximum * weight
}

data class CapAdaptiveInspectionMilestoneAdjuster(var cap: Double) : AdaptiveInspectionMilestoneFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveInspectionMilestone, value: Double): Double = minOf(value, cap)
}

data class PercentAdaptiveInspectionMilestoneAdjuster(var percent: Double) : AdaptiveInspectionMilestoneFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveInspectionMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryAdaptiveInspectionMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveInspectionMilestoneFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: AdaptiveInspectionMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive inspection milestone steps in order and keeps a trace of each. */
data class AdaptiveInspectionMilestoneEngine(
    var steps: MutableList<AdaptiveInspectionMilestoneFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveInspectionMilestone): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveInspectionMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveInspectionMilestoneEngine(): AdaptiveInspectionMilestoneEngine {
    return AdaptiveInspectionMilestoneEngine(
        steps = mutableListOf<AdaptiveInspectionMilestoneFactor>(
            AdaptiveInspectionMilestoneMaximumOffsetAdjuster(0.66),
            CapAdaptiveInspectionMilestoneAdjuster(25893.0),
            PercentAdaptiveInspectionMilestoneAdjuster(-12.6),
            CountryAdaptiveInspectionMilestoneAdjuster(mutableMapOf<String, Double>("north" to 0.97)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 66994.6,
    )
}
