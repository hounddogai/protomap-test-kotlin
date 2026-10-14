package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.kit.flags.Registry

/** One step of the weighted carbon report score calculation. */
interface WeightedCarbonReportScoreFactor {
    fun describe(): String
    fun adjust(item: WeightedCarbonReportScore, value: Double): Double
}

data class CapWeightedCarbonReportScoreAdjuster(var cap: Double) : WeightedCarbonReportScoreFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedCarbonReportScore, value: Double): Double = minOf(value, cap)
}

data class LabelWeightedCarbonReportScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedCarbonReportScoreFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: WeightedCarbonReportScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedWeightedCarbonReportScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedCarbonReportScoreFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedCarbonReportScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentWeightedCarbonReportScoreAdjuster(var percent: Double) : WeightedCarbonReportScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedCarbonReportScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredWeightedCarbonReportScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedCarbonReportScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedCarbonReportScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumWeightedCarbonReportScoreAdjuster(var minimum: Double) : WeightedCarbonReportScoreFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedCarbonReportScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted carbon report score steps in order and keeps a trace of each. */
data class WeightedCarbonReportScoreEngine(
    var steps: MutableList<WeightedCarbonReportScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedCarbonReportScore): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedCarbonReportScoreFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedCarbonReportScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedCarbonReportScoreEngine(flags: Registry): WeightedCarbonReportScoreEngine {
    return WeightedCarbonReportScoreEngine(
        steps = mutableListOf<WeightedCarbonReportScoreFactor>(
            CapWeightedCarbonReportScoreAdjuster(28012.0),
            LabelWeightedCarbonReportScoreAdjuster(
                mutableMapOf<String, Double>("south" to 1.3, "north" to 1.18, "online" to 1.02),
            ),
            FlaggedWeightedCarbonReportScoreAdjuster(flags, "weighted-carbon-report-score-rolling", -8.8),
            PercentWeightedCarbonReportScoreAdjuster(-6.1),
            TieredWeightedCarbonReportScoreAdjuster(
                mutableListOf<Double>(3154.0, 4210.0, 6003.0, 7333.0),
                mutableListOf<Double>(0.95, 0.96, 0.99, 0.96),
            ),
            MinimumWeightedCarbonReportScoreAdjuster(34.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 48938.1,
    )
}
