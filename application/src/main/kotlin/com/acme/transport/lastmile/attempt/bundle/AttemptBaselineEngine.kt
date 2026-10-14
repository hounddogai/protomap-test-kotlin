package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the attempt baseline calculation. */
interface AttemptBaselineAdjuster {
    fun describe(): String
    fun adjust(item: AttemptBaseline, value: Double): Double
}

data class PercentAttemptBaselineAdjuster(var percent: Double) : AttemptBaselineAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAttemptBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptBaselineAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingAttemptBaselineAdjuster(var step: Double) : AttemptBaselineAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AttemptBaseline, value: Double): Double = roundTo(value, step)
}

data class MinimumAttemptBaselineAdjuster(var minimum: Double) : AttemptBaselineAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AttemptBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAttemptBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptBaselineAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the attempt baseline steps in order and keeps a trace of each. */
data class AttemptBaselineEngine(
    var steps: MutableList<AttemptBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AttemptBaseline): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AttemptBaselineAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AttemptBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAttemptBaselineEngine(flags: Registry): AttemptBaselineEngine {
    return AttemptBaselineEngine(
        steps = mutableListOf<AttemptBaselineAdjuster>(
            PercentAttemptBaselineAdjuster(-14.8),
            TieredAttemptBaselineAdjuster(
                mutableListOf<Double>(5192.0, 6972.0, 7062.0),
                mutableListOf<Double>(1.04, 1.09, 1.18),
            ),
            RoundingAttemptBaselineAdjuster(0.1),
            MinimumAttemptBaselineAdjuster(13.6),
            FlaggedAttemptBaselineAdjuster(flags, "attempt-baseline-legacy", 1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 49814.5,
    )
}
