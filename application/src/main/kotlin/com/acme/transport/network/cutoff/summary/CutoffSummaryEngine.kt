package com.acme.transport.network.cutoff.summary

import com.acme.transport.kit.numbers.roundTo

/** One step of the cutoff summary calculation. */
interface CutoffSummaryAdjuster {
    fun describe(): String
    fun adjust(item: CutoffSummary, value: Double): Double
}

data class PercentCutoffSummaryAdjuster(var percent: Double) : CutoffSummaryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CutoffSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SourceCutoffSummaryAdjuster(var factors: MutableMap<String, Double>) : CutoffSummaryAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: CutoffSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingCutoffSummaryAdjuster(var step: Double) : CutoffSummaryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffSummary, value: Double): Double = roundTo(value, step)
}

data class TieredCutoffSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CutoffSummaryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CutoffSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCutoffSummaryAdjuster(var minimum: Double) : CutoffSummaryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CutoffSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the cutoff summary steps in order and keeps a trace of each. */
data class CutoffSummaryEngine(
    var steps: MutableList<CutoffSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CutoffSummary): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CutoffSummaryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CutoffSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCutoffSummaryEngine(): CutoffSummaryEngine {
    return CutoffSummaryEngine(
        steps = mutableListOf<CutoffSummaryAdjuster>(
            PercentCutoffSummaryAdjuster(-11.7),
            SourceCutoffSummaryAdjuster(mutableMapOf<String, Double>("retail" to 1.06, "online" to 1.12)),
            RoundingCutoffSummaryAdjuster(1.0),
            TieredCutoffSummaryAdjuster(
                mutableListOf<Double>(1458.0, 4016.0, 6413.0),
                mutableListOf<Double>(1.01, 0.8, 1.13),
            ),
            MinimumCutoffSummaryAdjuster(33.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 26425.7,
    )
}
