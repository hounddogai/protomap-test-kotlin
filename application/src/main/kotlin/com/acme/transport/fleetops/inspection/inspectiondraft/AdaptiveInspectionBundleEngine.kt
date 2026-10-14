package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive inspection bundle calculation. */
interface AdaptiveInspectionBundleStep {
    fun describe(): String
    fun adjust(item: AdaptiveInspectionBundle, value: Double): Double
}

data class CapAdaptiveInspectionBundleAdjuster(var cap: Double) : AdaptiveInspectionBundleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveInspectionBundle, value: Double): Double = minOf(value, cap)
}

data class PercentAdaptiveInspectionBundleAdjuster(var percent: Double) : AdaptiveInspectionBundleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveInspectionBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingAdaptiveInspectionBundleAdjuster(var step: Double) : AdaptiveInspectionBundleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveInspectionBundle, value: Double): Double = roundTo(value, step)
}

data class NameAdaptiveInspectionBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveInspectionBundleStep {
    override fun describe(): String = "by name"

    override fun adjust(item: AdaptiveInspectionBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAdaptiveInspectionBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveInspectionBundleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveInspectionBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AdaptiveInspectionBundleCeilingOffsetAdjuster(var weight: Double) : AdaptiveInspectionBundleStep {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: AdaptiveInspectionBundle, value: Double): Double = value + item.ceiling * weight
}

/** Runs the adaptive inspection bundle steps in order and keeps a trace of each. */
data class AdaptiveInspectionBundleEngine(
    var steps: MutableList<AdaptiveInspectionBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveInspectionBundle): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveInspectionBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveInspectionBundleEngine(): AdaptiveInspectionBundleEngine {
    return AdaptiveInspectionBundleEngine(
        steps = mutableListOf<AdaptiveInspectionBundleStep>(
            CapAdaptiveInspectionBundleAdjuster(3735.0),
            PercentAdaptiveInspectionBundleAdjuster(22.2),
            RoundingAdaptiveInspectionBundleAdjuster(0.05),
            NameAdaptiveInspectionBundleAdjuster(mutableMapOf<String, Double>("south" to 0.79)),
            TieredAdaptiveInspectionBundleAdjuster(
                mutableListOf<Double>(2695.0, 7720.0),
                mutableListOf<Double>(1.03, 0.88),
            ),
            AdaptiveInspectionBundleCeilingOffsetAdjuster(0.31),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 28847.4,
    )
}
