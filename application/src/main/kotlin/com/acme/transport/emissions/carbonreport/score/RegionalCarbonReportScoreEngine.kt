package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional carbon report score calculation. */
interface RegionalCarbonReportScoreFactor {
    fun describe(): String
    fun adjust(item: RegionalCarbonReportScore, value: Double): Double
}

data class RoundingRegionalCarbonReportScoreAdjuster(var step: Double) : RegionalCarbonReportScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalCarbonReportScore, value: Double): Double = roundTo(value, step)
}

data class LaneRegionalCarbonReportScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCarbonReportScoreFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: RegionalCarbonReportScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRegionalCarbonReportScoreAdjuster(var percent: Double) : RegionalCarbonReportScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalCarbonReportScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRegionalCarbonReportScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalCarbonReportScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalCarbonReportScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRegionalCarbonReportScoreAdjuster(var cap: Double) : RegionalCarbonReportScoreFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalCarbonReportScore, value: Double): Double = minOf(value, cap)
}

/** Runs the regional carbon report score steps in order and keeps a trace of each. */
data class RegionalCarbonReportScoreEngine(
    var steps: MutableList<RegionalCarbonReportScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalCarbonReportScore): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalCarbonReportScoreFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalCarbonReportScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalCarbonReportScoreEngine(): RegionalCarbonReportScoreEngine {
    return RegionalCarbonReportScoreEngine(
        steps = mutableListOf<RegionalCarbonReportScoreFactor>(
            RoundingRegionalCarbonReportScoreAdjuster(0.1),
            LaneRegionalCarbonReportScoreAdjuster(mutableMapOf<String, Double>("central" to 1.3)),
            PercentRegionalCarbonReportScoreAdjuster(-20.7),
            TieredRegionalCarbonReportScoreAdjuster(
                mutableListOf<Double>(3698.0, 7996.0),
                mutableListOf<Double>(1.09, 1.12),
            ),
            CapRegionalCarbonReportScoreAdjuster(4542.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 72832.3,
    )
}
