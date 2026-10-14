package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the idling baseline calculation. */
interface IdlingBaselineStep {
    fun describe(): String
    fun adjust(item: IdlingBaseline, value: Double): Double
}

data class CapIdlingBaselineAdjuster(var cap: Double) : IdlingBaselineStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingBaseline, value: Double): Double = minOf(value, cap)
}

data class RoundingIdlingBaselineAdjuster(var step: Double) : IdlingBaselineStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IdlingBaseline, value: Double): Double = roundTo(value, step)
}

data class FlaggedIdlingBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingBaselineStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumIdlingBaselineAdjuster(var minimum: Double) : IdlingBaselineStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the idling baseline steps in order and keeps a trace of each. */
data class IdlingBaselineEngine(
    var steps: MutableList<IdlingBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IdlingBaseline): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingBaselineStep = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIdlingBaselineEngine(flags: Registry): IdlingBaselineEngine {
    return IdlingBaselineEngine(
        steps = mutableListOf<IdlingBaselineStep>(
            CapIdlingBaselineAdjuster(5606.0),
            RoundingIdlingBaselineAdjuster(0.1),
            FlaggedIdlingBaselineAdjuster(flags, "idling-baseline-seasonal", 1.9),
            MinimumIdlingBaselineAdjuster(33.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 33714.6,
    )
}
