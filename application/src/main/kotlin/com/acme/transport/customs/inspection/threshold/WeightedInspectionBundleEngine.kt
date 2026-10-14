package com.acme.transport.customs.inspection.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted inspection bundle calculation. */
interface WeightedInspectionBundleModifier {
    fun describe(): String
    fun adjust(item: WeightedInspectionBundle, value: Double): Double
}

data class TieredWeightedInspectionBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedInspectionBundleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedInspectionBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapWeightedInspectionBundleAdjuster(var cap: Double) : WeightedInspectionBundleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedInspectionBundle, value: Double): Double = minOf(value, cap)
}

data class WeightedInspectionBundleCostOffsetAdjuster(var weight: Double) : WeightedInspectionBundleModifier {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: WeightedInspectionBundle, value: Double): Double = value + item.cost * weight
}

data class FlaggedWeightedInspectionBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedInspectionBundleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedInspectionBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentWeightedInspectionBundleAdjuster(var percent: Double) : WeightedInspectionBundleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedInspectionBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneWeightedInspectionBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedInspectionBundleModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: WeightedInspectionBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted inspection bundle steps in order and keeps a trace of each. */
data class WeightedInspectionBundleEngine(
    var steps: MutableList<WeightedInspectionBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedInspectionBundle): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedInspectionBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedInspectionBundleEngine(flags: Registry): WeightedInspectionBundleEngine {
    return WeightedInspectionBundleEngine(
        steps = mutableListOf<WeightedInspectionBundleModifier>(
            TieredWeightedInspectionBundleAdjuster(
                mutableListOf<Double>(2326.0, 5515.0, 9549.0),
                mutableListOf<Double>(0.92, 1.06, 1.0),
            ),
            CapWeightedInspectionBundleAdjuster(49706.0),
            WeightedInspectionBundleCostOffsetAdjuster(0.58),
            FlaggedWeightedInspectionBundleAdjuster(flags, "weighted-inspection-bundle-regional", 8.6),
            PercentWeightedInspectionBundleAdjuster(-20.6),
            LaneWeightedInspectionBundleAdjuster(
                mutableMapOf<String, Double>("online" to 1.01, "retail" to 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 47933.3,
    )
}
