package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the trailer yard override calculation. */
interface TrailerYardOverrideStep {
    fun describe(): String
    fun adjust(item: TrailerYardOverride, value: Double): Double
}

data class CapTrailerYardOverrideAdjuster(var cap: Double) : TrailerYardOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerYardOverride, value: Double): Double = minOf(value, cap)
}

data class RoundingTrailerYardOverrideAdjuster(var step: Double) : TrailerYardOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerYardOverride, value: Double): Double = roundTo(value, step)
}

data class TrailerYardOverrideMinimumOffsetAdjuster(var weight: Double) : TrailerYardOverrideStep {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: TrailerYardOverride, value: Double): Double = value + item.minimum * weight
}

data class TieredTrailerYardOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerYardOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerYardOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTrailerYardOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerYardOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerYardOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTrailerYardOverrideAdjuster(var minimum: Double) : TrailerYardOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerYardOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the trailer yard override steps in order and keeps a trace of each. */
data class TrailerYardOverrideEngine(
    var steps: MutableList<TrailerYardOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerYardOverride): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerYardOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerYardOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerYardOverrideEngine(flags: Registry): TrailerYardOverrideEngine {
    return TrailerYardOverrideEngine(
        steps = mutableListOf<TrailerYardOverrideStep>(
            CapTrailerYardOverrideAdjuster(43359.0),
            RoundingTrailerYardOverrideAdjuster(1.0),
            TrailerYardOverrideMinimumOffsetAdjuster(1.91),
            TieredTrailerYardOverrideAdjuster(
                mutableListOf<Double>(4714.0, 6128.0, 6524.0, 9376.0),
                mutableListOf<Double>(1.08, 0.97, 1.05, 0.92),
            ),
            FlaggedTrailerYardOverrideAdjuster(flags, "trailer-yard-override-weighted", -9.7),
            MinimumTrailerYardOverrideAdjuster(29.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 30303.5,
    )
}
