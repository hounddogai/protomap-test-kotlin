package com.acme.transport.terminal.traileryard.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default trailer yard window calculation. */
interface DefaultTrailerYardWindowModifier {
    fun describe(): String
    fun adjust(item: DefaultTrailerYardWindow, value: Double): Double
}

data class PercentDefaultTrailerYardWindowAdjuster(var percent: Double) : DefaultTrailerYardWindowModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultTrailerYardWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDefaultTrailerYardWindowAdjuster(var cap: Double) : DefaultTrailerYardWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultTrailerYardWindow, value: Double): Double = minOf(value, cap)
}

data class MinimumDefaultTrailerYardWindowAdjuster(var minimum: Double) : DefaultTrailerYardWindowModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultTrailerYardWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDefaultTrailerYardWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultTrailerYardWindowModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultTrailerYardWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDefaultTrailerYardWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultTrailerYardWindowModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultTrailerYardWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDefaultTrailerYardWindowAdjuster(var step: Double) : DefaultTrailerYardWindowModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultTrailerYardWindow, value: Double): Double = roundTo(value, step)
}

/** Runs the default trailer yard window steps in order and keeps a trace of each. */
data class DefaultTrailerYardWindowEngine(
    var steps: MutableList<DefaultTrailerYardWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultTrailerYardWindow): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultTrailerYardWindowModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultTrailerYardWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultTrailerYardWindowEngine(flags: Registry): DefaultTrailerYardWindowEngine {
    return DefaultTrailerYardWindowEngine(
        steps = mutableListOf<DefaultTrailerYardWindowModifier>(
            PercentDefaultTrailerYardWindowAdjuster(-20.1),
            CapDefaultTrailerYardWindowAdjuster(43500.0),
            MinimumDefaultTrailerYardWindowAdjuster(10.7),
            TieredDefaultTrailerYardWindowAdjuster(
                mutableListOf<Double>(3053.0, 4235.0, 7213.0),
                mutableListOf<Double>(0.98, 0.92, 0.98),
            ),
            FlaggedDefaultTrailerYardWindowAdjuster(flags, "default-trailer-yard-window-seasonal", 7.6),
            RoundingDefaultTrailerYardWindowAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 9417.1,
    )
}
