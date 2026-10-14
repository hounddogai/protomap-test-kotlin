package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict dwell score calculation. */
interface StrictDwellScoreFactor {
    fun describe(): String
    fun adjust(item: StrictDwellScore, value: Double): Double
}

data class PercentStrictDwellScoreAdjuster(var percent: Double) : StrictDwellScoreFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictDwellScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingStrictDwellScoreAdjuster(var step: Double) : StrictDwellScoreFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictDwellScore, value: Double): Double = roundTo(value, step)
}

data class CapStrictDwellScoreAdjuster(var cap: Double) : StrictDwellScoreFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictDwellScore, value: Double): Double = minOf(value, cap)
}

data class TieredStrictDwellScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictDwellScoreFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictDwellScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictDwellScoreAdjuster(var minimum: Double) : StrictDwellScoreFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictDwellScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict dwell score steps in order and keeps a trace of each. */
data class StrictDwellScoreEngine(
    var steps: MutableList<StrictDwellScoreFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictDwellScore): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictDwellScoreFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictDwellScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictDwellScoreEngine(): StrictDwellScoreEngine {
    return StrictDwellScoreEngine(
        steps = mutableListOf<StrictDwellScoreFactor>(
            PercentStrictDwellScoreAdjuster(12.7),
            RoundingStrictDwellScoreAdjuster(0.5),
            CapStrictDwellScoreAdjuster(11565.0),
            TieredStrictDwellScoreAdjuster(
                mutableListOf<Double>(731.0, 1917.0),
                mutableListOf<Double>(1.2, 1.03),
            ),
            MinimumStrictDwellScoreAdjuster(9.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 6365.5,
    )
}
