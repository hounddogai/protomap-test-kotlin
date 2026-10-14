package com.acme.transport.crewing.roster.summary

import com.acme.transport.kit.numbers.roundTo

/** One step of the roster summary calculation. */
interface RosterSummaryAdjuster {
    fun describe(): String
    fun adjust(item: RosterSummary, value: Double): Double
}

data class LabelRosterSummaryAdjuster(var factors: MutableMap<String, Double>) : RosterSummaryAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: RosterSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRosterSummaryAdjuster(var step: Double) : RosterSummaryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterSummary, value: Double): Double = roundTo(value, step)
}

data class MinimumRosterSummaryAdjuster(var minimum: Double) : RosterSummaryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RosterSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRosterSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RosterSummaryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RosterSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the roster summary steps in order and keeps a trace of each. */
data class RosterSummaryEngine(
    var steps: MutableList<RosterSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RosterSummary): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterSummaryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RosterSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRosterSummaryEngine(): RosterSummaryEngine {
    return RosterSummaryEngine(
        steps = mutableListOf<RosterSummaryAdjuster>(
            LabelRosterSummaryAdjuster(mutableMapOf<String, Double>("retail" to 1.28, "online" to 0.86)),
            RoundingRosterSummaryAdjuster(0.5),
            MinimumRosterSummaryAdjuster(1.7),
            TieredRosterSummaryAdjuster(
                mutableListOf<Double>(1900.0, 5760.0, 6756.0),
                mutableListOf<Double>(0.94, 0.85, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 61904.8,
    )
}
