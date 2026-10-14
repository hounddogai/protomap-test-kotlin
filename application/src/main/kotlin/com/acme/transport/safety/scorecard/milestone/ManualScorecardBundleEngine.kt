package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual scorecard bundle calculation. */
interface ManualScorecardBundleModifier {
    fun describe(): String
    fun adjust(item: ManualScorecardBundle, value: Double): Double
}

data class LabelManualScorecardBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualScorecardBundleModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: ManualScorecardBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredManualScorecardBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualScorecardBundleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualScorecardBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ManualScorecardBundleLoadKgOffsetAdjuster(var weight: Double) : ManualScorecardBundleModifier {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: ManualScorecardBundle, value: Double): Double = value + item.loadKg * weight
}

data class CapManualScorecardBundleAdjuster(var cap: Double) : ManualScorecardBundleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualScorecardBundle, value: Double): Double = minOf(value, cap)
}

/** Runs the manual scorecard bundle steps in order and keeps a trace of each. */
data class ManualScorecardBundleEngine(
    var steps: MutableList<ManualScorecardBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualScorecardBundle): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualScorecardBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualScorecardBundleEngine(): ManualScorecardBundleEngine {
    return ManualScorecardBundleEngine(
        steps = mutableListOf<ManualScorecardBundleModifier>(
            LabelManualScorecardBundleAdjuster(
                mutableMapOf<String, Double>("retail" to 1.15, "north" to 1.37, "east" to 1.02),
            ),
            TieredManualScorecardBundleAdjuster(
                mutableListOf<Double>(4644.0, 4861.0, 8640.0, 9277.0),
                mutableListOf<Double>(1.0, 1.08, 1.12, 1.18),
            ),
            ManualScorecardBundleLoadKgOffsetAdjuster(0.78),
            CapManualScorecardBundleAdjuster(13511.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 3173.4,
    )
}
