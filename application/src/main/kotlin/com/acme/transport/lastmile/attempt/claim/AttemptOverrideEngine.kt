package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the attempt override calculation. */
interface AttemptOverrideStep {
    fun describe(): String
    fun adjust(item: AttemptOverride, value: Double): Double
}

data class RoundingAttemptOverrideAdjuster(var step: Double) : AttemptOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AttemptOverride, value: Double): Double = roundTo(value, step)
}

data class MinimumAttemptOverrideAdjuster(var minimum: Double) : AttemptOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AttemptOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAttemptOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentAttemptOverrideAdjuster(var percent: Double) : AttemptOverrideStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AttemptOverrideLimitOffsetAdjuster(var weight: Double) : AttemptOverrideStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: AttemptOverride, value: Double): Double = value + item.limit.toDouble() * weight
}

data class CapAttemptOverrideAdjuster(var cap: Double) : AttemptOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AttemptOverride, value: Double): Double = minOf(value, cap)
}

/** Runs the attempt override steps in order and keeps a trace of each. */
data class AttemptOverrideEngine(
    var steps: MutableList<AttemptOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AttemptOverride): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AttemptOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<AttemptOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAttemptOverrideEngine(flags: Registry): AttemptOverrideEngine {
    return AttemptOverrideEngine(
        steps = mutableListOf<AttemptOverrideStep>(
            RoundingAttemptOverrideAdjuster(0.1),
            MinimumAttemptOverrideAdjuster(46.5),
            FlaggedAttemptOverrideAdjuster(flags, "attempt-override-global", 6.4),
            PercentAttemptOverrideAdjuster(-0.0),
            AttemptOverrideLimitOffsetAdjuster(1.9),
            CapAttemptOverrideAdjuster(1058.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 12020.7,
    )
}
