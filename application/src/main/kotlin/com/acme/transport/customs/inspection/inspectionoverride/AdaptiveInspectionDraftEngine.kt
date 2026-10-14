package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.flags.Registry

/** One step of the adaptive inspection draft calculation. */
interface AdaptiveInspectionDraftFactor {
    fun describe(): String
    fun adjust(item: AdaptiveInspectionDraft, value: Double): Double
}

data class FlaggedAdaptiveInspectionDraftAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveInspectionDraftFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveInspectionDraft, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAdaptiveInspectionDraftAdjuster(var cap: Double) : AdaptiveInspectionDraftFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveInspectionDraft, value: Double): Double = minOf(value, cap)
}

data class AdaptiveInspectionDraftLoadKgOffsetAdjuster(var weight: Double) : AdaptiveInspectionDraftFactor {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: AdaptiveInspectionDraft, value: Double): Double = value + item.loadKg * weight
}

data class TieredAdaptiveInspectionDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveInspectionDraftFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveInspectionDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive inspection draft steps in order and keeps a trace of each. */
data class AdaptiveInspectionDraftEngine(
    var steps: MutableList<AdaptiveInspectionDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveInspectionDraft): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveInspectionDraftFactor = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveInspectionDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveInspectionDraftEngine(flags: Registry): AdaptiveInspectionDraftEngine {
    return AdaptiveInspectionDraftEngine(
        steps = mutableListOf<AdaptiveInspectionDraftFactor>(
            FlaggedAdaptiveInspectionDraftAdjuster(flags, "adaptive-inspection-draft-manual", -9.7),
            CapAdaptiveInspectionDraftAdjuster(48429.0),
            AdaptiveInspectionDraftLoadKgOffsetAdjuster(1.8),
            TieredAdaptiveInspectionDraftAdjuster(
                mutableListOf<Double>(1902.0, 3620.0, 9886.0),
                mutableListOf<Double>(0.87, 1.0, 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 49048.7,
    )
}
