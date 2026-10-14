package com.acme.transport.safety.scorecard.scorecardwindow

/** One step of the lenient scorecard window calculation. */
interface LenientScorecardWindowAdjuster {
    fun describe(): String
    fun adjust(item: LenientScorecardWindow, value: Double): Double
}

data class CapLenientScorecardWindowAdjuster(var cap: Double) : LenientScorecardWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientScorecardWindow, value: Double): Double = minOf(value, cap)
}

data class TieredLenientScorecardWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientScorecardWindowAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientScorecardWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLenientScorecardWindowAdjuster(var minimum: Double) : LenientScorecardWindowAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientScorecardWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient scorecard window steps in order and keeps a trace of each. */
data class LenientScorecardWindowEngine(
    var steps: MutableList<LenientScorecardWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientScorecardWindow): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientScorecardWindowAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientScorecardWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientScorecardWindowEngine(): LenientScorecardWindowEngine {
    return LenientScorecardWindowEngine(
        steps = mutableListOf<LenientScorecardWindowAdjuster>(
            CapLenientScorecardWindowAdjuster(8362.0),
            TieredLenientScorecardWindowAdjuster(
                mutableListOf<Double>(1623.0, 4847.0, 7496.0),
                mutableListOf<Double>(1.04, 1.15, 1.03),
            ),
            MinimumLenientScorecardWindowAdjuster(12.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 34957.6,
    )
}
