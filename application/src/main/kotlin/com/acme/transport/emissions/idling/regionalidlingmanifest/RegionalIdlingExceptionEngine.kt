package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional idling exception calculation. */
interface RegionalIdlingExceptionStep {
    fun describe(): String
    fun adjust(item: RegionalIdlingException, value: Double): Double
}

data class CapRegionalIdlingExceptionAdjuster(var cap: Double) : RegionalIdlingExceptionStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalIdlingException, value: Double): Double = minOf(value, cap)
}

data class PercentRegionalIdlingExceptionAdjuster(var percent: Double) : RegionalIdlingExceptionStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalIdlingException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRegionalIdlingExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalIdlingExceptionStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalIdlingException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRegionalIdlingExceptionAdjuster(var step: Double) : RegionalIdlingExceptionStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalIdlingException, value: Double): Double = roundTo(value, step)
}

data class MinimumRegionalIdlingExceptionAdjuster(var minimum: Double) : RegionalIdlingExceptionStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalIdlingException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional idling exception steps in order and keeps a trace of each. */
data class RegionalIdlingExceptionEngine(
    var steps: MutableList<RegionalIdlingExceptionStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalIdlingException): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalIdlingExceptionStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalIdlingException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalIdlingExceptionEngine(flags: Registry): RegionalIdlingExceptionEngine {
    return RegionalIdlingExceptionEngine(
        steps = mutableListOf<RegionalIdlingExceptionStep>(
            CapRegionalIdlingExceptionAdjuster(6619.0),
            PercentRegionalIdlingExceptionAdjuster(-7.4),
            FlaggedRegionalIdlingExceptionAdjuster(flags, "regional-idling-exception-strict", -5.5),
            RoundingRegionalIdlingExceptionAdjuster(0.25),
            MinimumRegionalIdlingExceptionAdjuster(0.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 37911.4,
    )
}
