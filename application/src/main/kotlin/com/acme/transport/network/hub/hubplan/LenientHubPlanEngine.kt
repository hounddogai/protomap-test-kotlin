package com.acme.transport.network.hub.hubplan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lenient hub plan calculation. */
interface LenientHubPlanAdjuster {
    fun describe(): String
    fun adjust(item: LenientHubPlan, value: Double): Double
}

data class FlaggedLenientHubPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientHubPlanAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientHubPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLenientHubPlanAdjuster(var percent: Double) : LenientHubPlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientHubPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CategoryLenientHubPlanAdjuster(var factors: MutableMap<String, Double>) : LenientHubPlanAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: LenientHubPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapLenientHubPlanAdjuster(var cap: Double) : LenientHubPlanAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientHubPlan, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient hub plan steps in order and keeps a trace of each. */
data class LenientHubPlanEngine(
    var steps: MutableList<LenientHubPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientHubPlan): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientHubPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientHubPlanEngine(flags: Registry): LenientHubPlanEngine {
    return LenientHubPlanEngine(
        steps = mutableListOf<LenientHubPlanAdjuster>(
            FlaggedLenientHubPlanAdjuster(flags, "lenient-hub-plan-pending", 3.8),
            PercentLenientHubPlanAdjuster(8.3),
            CategoryLenientHubPlanAdjuster(mutableMapOf<String, Double>("retail" to 1.36, "south" to 0.98)),
            CapLenientHubPlanAdjuster(19422.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 3237.2,
    )
}
