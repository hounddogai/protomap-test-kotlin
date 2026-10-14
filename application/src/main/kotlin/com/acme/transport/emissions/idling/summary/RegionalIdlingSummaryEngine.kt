package com.acme.transport.emissions.idling.summary

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional idling summary calculation. */
interface RegionalIdlingSummaryModifier {
    fun describe(): String
    fun adjust(item: RegionalIdlingSummary, value: Double): Double
}

data class RoundingRegionalIdlingSummaryAdjuster(var step: Double) : RegionalIdlingSummaryModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalIdlingSummary, value: Double): Double = roundTo(value, step)
}

data class TieredRegionalIdlingSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalIdlingSummaryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalIdlingSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRegionalIdlingSummaryAdjuster(var percent: Double) : RegionalIdlingSummaryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalIdlingSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional idling summary steps in order and keeps a trace of each. */
data class RegionalIdlingSummaryEngine(
    var steps: MutableList<RegionalIdlingSummaryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalIdlingSummary): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalIdlingSummaryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalIdlingSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalIdlingSummaryEngine(): RegionalIdlingSummaryEngine {
    return RegionalIdlingSummaryEngine(
        steps = mutableListOf<RegionalIdlingSummaryModifier>(
            RoundingRegionalIdlingSummaryAdjuster(0.25),
            TieredRegionalIdlingSummaryAdjuster(
                mutableListOf<Double>(1514.0, 1970.0),
                mutableListOf<Double>(0.85, 1.0),
            ),
            PercentRegionalIdlingSummaryAdjuster(-21.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 89898.0,
    )
}
