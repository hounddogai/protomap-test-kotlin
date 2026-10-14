package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the dwell score calculation. */
interface DwellScoreModifier {
    fun describe(): String
    fun adjust(item: DwellScore, value: Double): Double
}

data class TieredDwellScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DwellScoreModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DwellScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDwellScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellScoreModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDwellScoreAdjuster(var step: Double) : DwellScoreModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellScore, value: Double): Double = roundTo(value, step)
}

data class MinimumDwellScoreAdjuster(var minimum: Double) : DwellScoreModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DwellScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the dwell score steps in order and keeps a trace of each. */
data class DwellScoreEngine(
    var steps: MutableList<DwellScoreModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DwellScore): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DwellScoreModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DwellScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDwellScoreEngine(flags: Registry): DwellScoreEngine {
    return DwellScoreEngine(
        steps = mutableListOf<DwellScoreModifier>(
            TieredDwellScoreAdjuster(
                mutableListOf<Double>(1723.0, 7004.0, 7086.0),
                mutableListOf<Double>(0.95, 0.94, 1.19),
            ),
            FlaggedDwellScoreAdjuster(flags, "dwell-score-weighted", 8.8),
            RoundingDwellScoreAdjuster(0.25),
            MinimumDwellScoreAdjuster(5.3),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 64525.6,
    )
}
