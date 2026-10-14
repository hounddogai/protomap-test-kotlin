package com.acme.transport.emissions.routeoffset.offsetnotice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default route offset baseline calculation. */
interface DefaultRouteOffsetBaselineModifier {
    fun describe(): String
    fun adjust(item: DefaultRouteOffsetBaseline, value: Double): Double
}

data class DefaultRouteOffsetBaselineSequenceOffsetAdjuster(
    var weight: Double,
) : DefaultRouteOffsetBaselineModifier {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: DefaultRouteOffsetBaseline, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class CapDefaultRouteOffsetBaselineAdjuster(var cap: Double) : DefaultRouteOffsetBaselineModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultRouteOffsetBaseline, value: Double): Double = minOf(value, cap)
}

data class RoundingDefaultRouteOffsetBaselineAdjuster(var step: Double) : DefaultRouteOffsetBaselineModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultRouteOffsetBaseline, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultRouteOffsetBaselineAdjuster(var minimum: Double) : DefaultRouteOffsetBaselineModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultRouteOffsetBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDefaultRouteOffsetBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultRouteOffsetBaselineModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultRouteOffsetBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDefaultRouteOffsetBaselineAdjuster(var percent: Double) : DefaultRouteOffsetBaselineModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultRouteOffsetBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default route offset baseline steps in order and keeps a trace of each. */
data class DefaultRouteOffsetBaselineEngine(
    var steps: MutableList<DefaultRouteOffsetBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultRouteOffsetBaseline): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultRouteOffsetBaselineModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultRouteOffsetBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultRouteOffsetBaselineEngine(flags: Registry): DefaultRouteOffsetBaselineEngine {
    return DefaultRouteOffsetBaselineEngine(
        steps = mutableListOf<DefaultRouteOffsetBaselineModifier>(
            DefaultRouteOffsetBaselineSequenceOffsetAdjuster(0.65),
            CapDefaultRouteOffsetBaselineAdjuster(37365.0),
            RoundingDefaultRouteOffsetBaselineAdjuster(0.1),
            MinimumDefaultRouteOffsetBaselineAdjuster(28.6),
            FlaggedDefaultRouteOffsetBaselineAdjuster(flags, "default-route-offset-baseline-regional", 6.7),
            PercentDefaultRouteOffsetBaselineAdjuster(-2.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 81888.8,
    )
}
