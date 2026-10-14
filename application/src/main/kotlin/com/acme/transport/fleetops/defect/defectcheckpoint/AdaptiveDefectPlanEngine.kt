package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive defect plan calculation. */
interface AdaptiveDefectPlanFactor {
    fun describe(): String
    fun adjust(item: AdaptiveDefectPlan, value: Double): Double
}

data class FlaggedAdaptiveDefectPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveDefectPlanFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveDefectPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAdaptiveDefectPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveDefectPlanFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveDefectPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAdaptiveDefectPlanAdjuster(var minimum: Double) : AdaptiveDefectPlanFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveDefectPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive defect plan steps in order and keeps a trace of each. */
data class AdaptiveDefectPlanEngine(
    var steps: MutableList<AdaptiveDefectPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveDefectPlan): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveDefectPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveDefectPlanEngine(flags: Registry): AdaptiveDefectPlanEngine {
    return AdaptiveDefectPlanEngine(
        steps = mutableListOf<AdaptiveDefectPlanFactor>(
            FlaggedAdaptiveDefectPlanAdjuster(flags, "adaptive-defect-plan-weighted", 0.6),
            TieredAdaptiveDefectPlanAdjuster(
                mutableListOf<Double>(3466.0, 5977.0, 7537.0, 9808.0),
                mutableListOf<Double>(0.87, 0.91, 0.81, 1.09),
            ),
            MinimumAdaptiveDefectPlanAdjuster(31.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 35606.5,
    )
}
