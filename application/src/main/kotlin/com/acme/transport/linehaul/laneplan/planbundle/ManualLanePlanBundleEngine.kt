package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual lane plan bundle calculation. */
interface ManualLanePlanBundleModifier {
    fun describe(): String
    fun adjust(item: ManualLanePlanBundle, value: Double): Double
}

data class MinimumManualLanePlanBundleAdjuster(var minimum: Double) : ManualLanePlanBundleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualLanePlanBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingManualLanePlanBundleAdjuster(var step: Double) : ManualLanePlanBundleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualLanePlanBundle, value: Double): Double = roundTo(value, step)
}

data class ManualLanePlanBundleRatioOffsetAdjuster(var weight: Double) : ManualLanePlanBundleModifier {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: ManualLanePlanBundle, value: Double): Double = value + item.ratio * weight
}

data class TieredManualLanePlanBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualLanePlanBundleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualLanePlanBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual lane plan bundle steps in order and keeps a trace of each. */
data class ManualLanePlanBundleEngine(
    var steps: MutableList<ManualLanePlanBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualLanePlanBundle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualLanePlanBundleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualLanePlanBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualLanePlanBundleEngine(): ManualLanePlanBundleEngine {
    return ManualLanePlanBundleEngine(
        steps = mutableListOf<ManualLanePlanBundleModifier>(
            MinimumManualLanePlanBundleAdjuster(33.2),
            RoundingManualLanePlanBundleAdjuster(5.0),
            ManualLanePlanBundleRatioOffsetAdjuster(1.45),
            TieredManualLanePlanBundleAdjuster(
                mutableListOf<Double>(3433.0, 7440.0, 9223.0),
                mutableListOf<Double>(1.0, 1.09, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 66620.2,
    )
}
