package com.acme.transport.linehaul.trailer.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the trailer digest calculation. */
interface TrailerDigestStep {
    fun describe(): String
    fun adjust(item: TrailerDigest, value: Double): Double
}

data class RoundingTrailerDigestAdjuster(var step: Double) : TrailerDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerDigest, value: Double): Double = roundTo(value, step)
}

data class TieredTrailerDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumTrailerDigestAdjuster(var minimum: Double) : TrailerDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTrailerDigestAdjuster(var cap: Double) : TrailerDigestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerDigest, value: Double): Double = minOf(value, cap)
}

data class TrailerDigestGraceDaysOffsetAdjuster(var weight: Double) : TrailerDigestStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: TrailerDigest, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class FlaggedTrailerDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the trailer digest steps in order and keeps a trace of each. */
data class TrailerDigestEngine(
    var steps: MutableList<TrailerDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TrailerDigest): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerDigestStep = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTrailerDigestEngine(flags: Registry): TrailerDigestEngine {
    return TrailerDigestEngine(
        steps = mutableListOf<TrailerDigestStep>(
            RoundingTrailerDigestAdjuster(1.0),
            TieredTrailerDigestAdjuster(mutableListOf<Double>(314.0, 1233.0), mutableListOf<Double>(1.01, 1.09)),
            MinimumTrailerDigestAdjuster(29.1),
            CapTrailerDigestAdjuster(13632.0),
            TrailerDigestGraceDaysOffsetAdjuster(0.63),
            FlaggedTrailerDigestAdjuster(flags, "trailer-digest-adaptive", 0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 65161.3,
    )
}
