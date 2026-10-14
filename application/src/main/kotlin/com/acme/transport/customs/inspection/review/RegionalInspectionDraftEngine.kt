package com.acme.transport.customs.inspection.review

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional inspection draft calculation. */
interface RegionalInspectionDraftStep {
    fun describe(): String
    fun adjust(item: RegionalInspectionDraft, value: Double): Double
}

data class ChannelRegionalInspectionDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalInspectionDraftStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: RegionalInspectionDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRegionalInspectionDraftAdjuster(var step: Double) : RegionalInspectionDraftStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalInspectionDraft, value: Double): Double = roundTo(value, step)
}

data class RegionalInspectionDraftMaximumOffsetAdjuster(var weight: Double) : RegionalInspectionDraftStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: RegionalInspectionDraft, value: Double): Double = value + item.maximum * weight
}

data class CapRegionalInspectionDraftAdjuster(var cap: Double) : RegionalInspectionDraftStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalInspectionDraft, value: Double): Double = minOf(value, cap)
}

data class TieredRegionalInspectionDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalInspectionDraftStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalInspectionDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional inspection draft steps in order and keeps a trace of each. */
data class RegionalInspectionDraftEngine(
    var steps: MutableList<RegionalInspectionDraftStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalInspectionDraft): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalInspectionDraftStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalInspectionDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalInspectionDraftEngine(): RegionalInspectionDraftEngine {
    return RegionalInspectionDraftEngine(
        steps = mutableListOf<RegionalInspectionDraftStep>(
            ChannelRegionalInspectionDraftAdjuster(mutableMapOf<String, Double>("east" to 1.03)),
            RoundingRegionalInspectionDraftAdjuster(1.0),
            RegionalInspectionDraftMaximumOffsetAdjuster(0.06),
            CapRegionalInspectionDraftAdjuster(12414.0),
            TieredRegionalInspectionDraftAdjuster(
                mutableListOf<Double>(29.0, 7000.0, 8067.0, 8178.0),
                mutableListOf<Double>(1.01, 1.04, 1.04, 1.03),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 14532.4,
    )
}
