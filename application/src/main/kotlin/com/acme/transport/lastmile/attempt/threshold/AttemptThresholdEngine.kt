package com.acme.transport.lastmile.attempt.threshold

import com.acme.transport.kit.flags.Registry

/** One step of the attempt threshold calculation. */
interface AttemptThresholdModifier {
    fun describe(): String
    fun adjust(item: AttemptThreshold, value: Double): Double
}

data class PercentAttemptThresholdAdjuster(var percent: Double) : AttemptThresholdModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AttemptThresholdWindowDaysOffsetAdjuster(var weight: Double) : AttemptThresholdModifier {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: AttemptThreshold, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class MinimumAttemptThresholdAdjuster(var minimum: Double) : AttemptThresholdModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AttemptThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAttemptThresholdAdjuster(var cap: Double) : AttemptThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AttemptThreshold, value: Double): Double = minOf(value, cap)
}

data class UnitAttemptThresholdAdjuster(var factors: MutableMap<String, Double>) : AttemptThresholdModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: AttemptThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedAttemptThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the attempt threshold steps in order and keeps a trace of each. */
data class AttemptThresholdEngine(
    var steps: MutableList<AttemptThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AttemptThreshold): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AttemptThresholdModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AttemptThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAttemptThresholdEngine(flags: Registry): AttemptThresholdEngine {
    return AttemptThresholdEngine(
        steps = mutableListOf<AttemptThresholdModifier>(
            PercentAttemptThresholdAdjuster(9.0),
            AttemptThresholdWindowDaysOffsetAdjuster(1.48),
            MinimumAttemptThresholdAdjuster(13.6),
            CapAttemptThresholdAdjuster(20990.0),
            UnitAttemptThresholdAdjuster(mutableMapOf<String, Double>("central" to 1.36)),
            FlaggedAttemptThresholdAdjuster(flags, "attempt-threshold-fallback", -9.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 85400.1,
    )
}
