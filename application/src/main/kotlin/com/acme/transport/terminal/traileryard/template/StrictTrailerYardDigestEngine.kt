package com.acme.transport.terminal.traileryard.template

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict trailer yard digest calculation. */
interface StrictTrailerYardDigestFactor {
    fun describe(): String
    fun adjust(item: StrictTrailerYardDigest, value: Double): Double
}

data class PercentStrictTrailerYardDigestAdjuster(var percent: Double) : StrictTrailerYardDigestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictTrailerYardDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapStrictTrailerYardDigestAdjuster(var cap: Double) : StrictTrailerYardDigestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictTrailerYardDigest, value: Double): Double = minOf(value, cap)
}

data class MinimumStrictTrailerYardDigestAdjuster(var minimum: Double) : StrictTrailerYardDigestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictTrailerYardDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingStrictTrailerYardDigestAdjuster(var step: Double) : StrictTrailerYardDigestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictTrailerYardDigest, value: Double): Double = roundTo(value, step)
}

data class FlaggedStrictTrailerYardDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictTrailerYardDigestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictTrailerYardDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredStrictTrailerYardDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictTrailerYardDigestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictTrailerYardDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict trailer yard digest steps in order and keeps a trace of each. */
data class StrictTrailerYardDigestEngine(
    var steps: MutableList<StrictTrailerYardDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictTrailerYardDigest): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictTrailerYardDigestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictTrailerYardDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictTrailerYardDigestEngine(flags: Registry): StrictTrailerYardDigestEngine {
    return StrictTrailerYardDigestEngine(
        steps = mutableListOf<StrictTrailerYardDigestFactor>(
            PercentStrictTrailerYardDigestAdjuster(3.9),
            CapStrictTrailerYardDigestAdjuster(22997.0),
            MinimumStrictTrailerYardDigestAdjuster(17.5),
            RoundingStrictTrailerYardDigestAdjuster(5.0),
            FlaggedStrictTrailerYardDigestAdjuster(flags, "strict-trailer-yard-digest-pending", -6.8),
            TieredStrictTrailerYardDigestAdjuster(
                mutableListOf<Double>(3371.0, 5306.0, 7367.0),
                mutableListOf<Double>(1.19, 1.18, 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 71208.8,
    )
}
