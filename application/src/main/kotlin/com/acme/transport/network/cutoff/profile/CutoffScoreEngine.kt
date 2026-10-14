package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the cutoff score calculation. */
interface CutoffScoreModifier {
    fun describe(): String
    fun adjust(item: CutoffScore, value: Double): Double
}

data class MinimumCutoffScoreAdjuster(var minimum: Double) : CutoffScoreModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CutoffScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCutoffScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CutoffScoreModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CutoffScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCutoffScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CutoffScoreModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CutoffScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingCutoffScoreAdjuster(var step: Double) : CutoffScoreModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffScore, value: Double): Double = roundTo(value, step)
}

/** Runs the cutoff score steps in order and keeps a trace of each. */
data class CutoffScoreEngine(
    var steps: MutableList<CutoffScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CutoffScore): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CutoffScoreModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CutoffScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCutoffScoreEngine(flags: Registry): CutoffScoreEngine {
    return CutoffScoreEngine(
        steps = mutableListOf<CutoffScoreModifier>(
            MinimumCutoffScoreAdjuster(20.5),
            TieredCutoffScoreAdjuster(
                mutableListOf<Double>(933.0, 7678.0, 9772.0),
                mutableListOf<Double>(1.01, 1.19, 1.0),
            ),
            FlaggedCutoffScoreAdjuster(flags, "cutoff-score-manual", -6.3),
            RoundingCutoffScoreAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 85401.2,
    )
}
