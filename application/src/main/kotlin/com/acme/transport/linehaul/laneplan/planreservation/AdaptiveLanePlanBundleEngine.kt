package com.acme.transport.linehaul.laneplan.planreservation

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive lane plan bundle calculation. */
interface AdaptiveLanePlanBundleModifier {
    fun describe(): String
    fun adjust(item: AdaptiveLanePlanBundle, value: Double): Double
}

data class CapAdaptiveLanePlanBundleAdjuster(var cap: Double) : AdaptiveLanePlanBundleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveLanePlanBundle, value: Double): Double = minOf(value, cap)
}

data class MinimumAdaptiveLanePlanBundleAdjuster(var minimum: Double) : AdaptiveLanePlanBundleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveLanePlanBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AdaptiveLanePlanBundleStopCountOffsetAdjuster(var weight: Double) : AdaptiveLanePlanBundleModifier {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: AdaptiveLanePlanBundle, value: Double): Double {
        return value + item.stopCount.toDouble() * weight
    }
}

/** Runs the adaptive lane plan bundle steps in order and keeps a trace of each. */
data class AdaptiveLanePlanBundleEngine(
    var steps: MutableList<AdaptiveLanePlanBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveLanePlanBundle): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveLanePlanBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveLanePlanBundleEngine(): AdaptiveLanePlanBundleEngine {
    return AdaptiveLanePlanBundleEngine(
        steps = mutableListOf<AdaptiveLanePlanBundleModifier>(
            CapAdaptiveLanePlanBundleAdjuster(45674.0),
            MinimumAdaptiveLanePlanBundleAdjuster(27.5),
            AdaptiveLanePlanBundleStopCountOffsetAdjuster(0.04),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 17401.0,
    )
}
