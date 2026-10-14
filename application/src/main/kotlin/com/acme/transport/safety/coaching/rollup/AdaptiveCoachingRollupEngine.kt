package com.acme.transport.safety.coaching.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive coaching rollup calculation. */
interface AdaptiveCoachingRollupModifier {
    fun describe(): String
    fun adjust(item: AdaptiveCoachingRollup, value: Double): Double
}

data class PercentAdaptiveCoachingRollupAdjuster(var percent: Double) : AdaptiveCoachingRollupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveCoachingRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingAdaptiveCoachingRollupAdjuster(var step: Double) : AdaptiveCoachingRollupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveCoachingRollup, value: Double): Double = roundTo(value, step)
}

data class FlaggedAdaptiveCoachingRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveCoachingRollupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveCoachingRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAdaptiveCoachingRollupAdjuster(var minimum: Double) : AdaptiveCoachingRollupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveCoachingRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive coaching rollup steps in order and keeps a trace of each. */
data class AdaptiveCoachingRollupEngine(
    var steps: MutableList<AdaptiveCoachingRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveCoachingRollup): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveCoachingRollupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveCoachingRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveCoachingRollupEngine(flags: Registry): AdaptiveCoachingRollupEngine {
    return AdaptiveCoachingRollupEngine(
        steps = mutableListOf<AdaptiveCoachingRollupModifier>(
            PercentAdaptiveCoachingRollupAdjuster(-24.0),
            RoundingAdaptiveCoachingRollupAdjuster(5.0),
            FlaggedAdaptiveCoachingRollupAdjuster(flags, "adaptive-coaching-rollup-scheduled", 3.0),
            MinimumAdaptiveCoachingRollupAdjuster(27.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 81638.7,
    )
}
