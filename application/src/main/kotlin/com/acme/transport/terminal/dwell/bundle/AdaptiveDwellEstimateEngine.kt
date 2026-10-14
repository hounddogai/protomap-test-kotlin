package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive dwell estimate calculation. */
interface AdaptiveDwellEstimateModifier {
    fun describe(): String
    fun adjust(item: AdaptiveDwellEstimate, value: Double): Double
}

data class FlaggedAdaptiveDwellEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveDwellEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveDwellEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class AdaptiveDwellEstimateDiscountOffsetAdjuster(var weight: Double) : AdaptiveDwellEstimateModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: AdaptiveDwellEstimate, value: Double): Double = value + item.discount * weight
}

data class RoundingAdaptiveDwellEstimateAdjuster(var step: Double) : AdaptiveDwellEstimateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveDwellEstimate, value: Double): Double = roundTo(value, step)
}

data class MinimumAdaptiveDwellEstimateAdjuster(var minimum: Double) : AdaptiveDwellEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveDwellEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive dwell estimate steps in order and keeps a trace of each. */
data class AdaptiveDwellEstimateEngine(
    var steps: MutableList<AdaptiveDwellEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveDwellEstimate): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveDwellEstimateModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveDwellEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveDwellEstimateEngine(flags: Registry): AdaptiveDwellEstimateEngine {
    return AdaptiveDwellEstimateEngine(
        steps = mutableListOf<AdaptiveDwellEstimateModifier>(
            FlaggedAdaptiveDwellEstimateAdjuster(flags, "adaptive-dwell-estimate-default", 7.9),
            AdaptiveDwellEstimateDiscountOffsetAdjuster(0.6),
            RoundingAdaptiveDwellEstimateAdjuster(0.1),
            MinimumAdaptiveDwellEstimateAdjuster(8.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 89332.8,
    )
}
