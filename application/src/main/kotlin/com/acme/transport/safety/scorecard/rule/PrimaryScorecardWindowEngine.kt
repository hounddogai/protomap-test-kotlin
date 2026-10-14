package com.acme.transport.safety.scorecard.rule

import com.acme.transport.kit.numbers.roundTo

/** One step of the primary scorecard window calculation. */
interface PrimaryScorecardWindowStep {
    fun describe(): String
    fun adjust(item: PrimaryScorecardWindow, value: Double): Double
}

data class PercentPrimaryScorecardWindowAdjuster(var percent: Double) : PrimaryScorecardWindowStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryScorecardWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingPrimaryScorecardWindowAdjuster(var step: Double) : PrimaryScorecardWindowStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryScorecardWindow, value: Double): Double = roundTo(value, step)
}

data class TieredPrimaryScorecardWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryScorecardWindowStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryScorecardWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPrimaryScorecardWindowAdjuster(var minimum: Double) : PrimaryScorecardWindowStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryScorecardWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PrimaryScorecardWindowBudgetOffsetAdjuster(var weight: Double) : PrimaryScorecardWindowStep {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: PrimaryScorecardWindow, value: Double): Double = value + item.budget * weight
}

data class CategoryPrimaryScorecardWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryScorecardWindowStep {
    override fun describe(): String = "by category"

    override fun adjust(item: PrimaryScorecardWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the primary scorecard window steps in order and keeps a trace of each. */
data class PrimaryScorecardWindowEngine(
    var steps: MutableList<PrimaryScorecardWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryScorecardWindow): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryScorecardWindowStep = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryScorecardWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryScorecardWindowEngine(): PrimaryScorecardWindowEngine {
    return PrimaryScorecardWindowEngine(
        steps = mutableListOf<PrimaryScorecardWindowStep>(
            PercentPrimaryScorecardWindowAdjuster(-5.4),
            RoundingPrimaryScorecardWindowAdjuster(5.0),
            TieredPrimaryScorecardWindowAdjuster(
                mutableListOf<Double>(150.0, 1769.0, 4446.0, 5413.0),
                mutableListOf<Double>(0.91, 1.15, 0.96, 1.16),
            ),
            MinimumPrimaryScorecardWindowAdjuster(45.9),
            PrimaryScorecardWindowBudgetOffsetAdjuster(0.9),
            CategoryPrimaryScorecardWindowAdjuster(
                mutableMapOf<String, Double>("east" to 0.84, "retail" to 1.31, "central" to 1.34),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 74365.8,
    )
}
