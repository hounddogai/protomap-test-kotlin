package com.acme.transport.customs.inspection.plan

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive inspection threshold calculation. */
interface AdaptiveInspectionThresholdStep {
    fun describe(): String
    fun adjust(item: AdaptiveInspectionThreshold, value: Double): Double
}

data class TieredAdaptiveInspectionThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveInspectionThresholdStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveInspectionThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAdaptiveInspectionThresholdAdjuster(var cap: Double) : AdaptiveInspectionThresholdStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveInspectionThreshold, value: Double): Double = minOf(value, cap)
}

data class FlaggedAdaptiveInspectionThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveInspectionThresholdStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveInspectionThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAdaptiveInspectionThresholdAdjuster(var minimum: Double) : AdaptiveInspectionThresholdStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveInspectionThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive inspection threshold steps in order and keeps a trace of each. */
data class AdaptiveInspectionThresholdEngine(
    var steps: MutableList<AdaptiveInspectionThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveInspectionThreshold): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveInspectionThresholdStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveInspectionThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveInspectionThresholdEngine(flags: Registry): AdaptiveInspectionThresholdEngine {
    return AdaptiveInspectionThresholdEngine(
        steps = mutableListOf<AdaptiveInspectionThresholdStep>(
            TieredAdaptiveInspectionThresholdAdjuster(
                mutableListOf<Double>(3649.0, 7674.0),
                mutableListOf<Double>(0.95, 1.1),
            ),
            CapAdaptiveInspectionThresholdAdjuster(5932.0),
            FlaggedAdaptiveInspectionThresholdAdjuster(flags, "adaptive-inspection-threshold-default", -8.3),
            MinimumAdaptiveInspectionThresholdAdjuster(27.2),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 48420.1,
    )
}
