package com.acme.transport.customs.inspection.target

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive inspection estimate calculation. */
interface AdaptiveInspectionEstimateAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveInspectionEstimate, value: Double): Double
}

data class TieredAdaptiveInspectionEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveInspectionEstimateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveInspectionEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SourceAdaptiveInspectionEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveInspectionEstimateAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: AdaptiveInspectionEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAdaptiveInspectionEstimateAdjuster(var percent: Double) : AdaptiveInspectionEstimateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveInspectionEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAdaptiveInspectionEstimateAdjuster(var cap: Double) : AdaptiveInspectionEstimateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveInspectionEstimate, value: Double): Double = minOf(value, cap)
}

data class MinimumAdaptiveInspectionEstimateAdjuster(var minimum: Double) : AdaptiveInspectionEstimateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveInspectionEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAdaptiveInspectionEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveInspectionEstimateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveInspectionEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the adaptive inspection estimate steps in order and keeps a trace of each. */
data class AdaptiveInspectionEstimateEngine(
    var steps: MutableList<AdaptiveInspectionEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveInspectionEstimate): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveInspectionEstimateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveInspectionEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveInspectionEstimateEngine(flags: Registry): AdaptiveInspectionEstimateEngine {
    return AdaptiveInspectionEstimateEngine(
        steps = mutableListOf<AdaptiveInspectionEstimateAdjuster>(
            TieredAdaptiveInspectionEstimateAdjuster(
                mutableListOf<Double>(4767.0, 5576.0, 6969.0, 9570.0),
                mutableListOf<Double>(1.17, 0.98, 1.18, 0.82),
            ),
            SourceAdaptiveInspectionEstimateAdjuster(mutableMapOf<String, Double>("retail" to 0.73)),
            PercentAdaptiveInspectionEstimateAdjuster(-11.0),
            CapAdaptiveInspectionEstimateAdjuster(2880.0),
            MinimumAdaptiveInspectionEstimateAdjuster(24.1),
            FlaggedAdaptiveInspectionEstimateAdjuster(flags, "adaptive-inspection-estimate-strict", 9.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 44765.0,
    )
}
