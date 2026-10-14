package com.acme.transport.fleetops.serviceplan.planpreference

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global service plan preference calculation. */
interface GlobalServicePlanPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: GlobalServicePlanPreference, value: Double): Double
}

data class RoundingGlobalServicePlanPreferenceAdjuster(var step: Double) : GlobalServicePlanPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalServicePlanPreference, value: Double): Double = roundTo(value, step)
}

data class GlobalServicePlanPreferenceScoreOffsetAdjuster(
    var weight: Double,
) : GlobalServicePlanPreferenceAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: GlobalServicePlanPreference, value: Double): Double = value + item.score * weight
}

data class PercentGlobalServicePlanPreferenceAdjuster(
    var percent: Double,
) : GlobalServicePlanPreferenceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalServicePlanPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the global service plan preference steps in order and keeps a trace of each. */
data class GlobalServicePlanPreferenceEngine(
    var steps: MutableList<GlobalServicePlanPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: GlobalServicePlanPreference): Double {
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

    fun evaluateAll(items: MutableList<GlobalServicePlanPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultGlobalServicePlanPreferenceEngine(): GlobalServicePlanPreferenceEngine {
    return GlobalServicePlanPreferenceEngine(
        steps = mutableListOf<GlobalServicePlanPreferenceAdjuster>(
            RoundingGlobalServicePlanPreferenceAdjuster(0.05),
            GlobalServicePlanPreferenceScoreOffsetAdjuster(0.84),
            PercentGlobalServicePlanPreferenceAdjuster(3.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 56528.0,
    )
}
