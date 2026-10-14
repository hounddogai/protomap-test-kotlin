package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive swap window calculation. */
interface AdaptiveSwapWindowStep {
    fun describe(): String
    fun adjust(item: AdaptiveSwapWindow, value: Double): Double
}

data class AdaptiveSwapWindowPriorityOffsetAdjuster(var weight: Double) : AdaptiveSwapWindowStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: AdaptiveSwapWindow, value: Double): Double = value + item.priority.toDouble() * weight
}

data class FlaggedAdaptiveSwapWindowAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveSwapWindowStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveSwapWindow, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAdaptiveSwapWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveSwapWindowStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveSwapWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingAdaptiveSwapWindowAdjuster(var step: Double) : AdaptiveSwapWindowStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveSwapWindow, value: Double): Double = roundTo(value, step)
}

data class PercentAdaptiveSwapWindowAdjuster(var percent: Double) : AdaptiveSwapWindowStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveSwapWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the adaptive swap window steps in order and keeps a trace of each. */
data class AdaptiveSwapWindowEngine(
    var steps: MutableList<AdaptiveSwapWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveSwapWindow): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveSwapWindowStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveSwapWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveSwapWindowEngine(flags: Registry): AdaptiveSwapWindowEngine {
    return AdaptiveSwapWindowEngine(
        steps = mutableListOf<AdaptiveSwapWindowStep>(
            AdaptiveSwapWindowPriorityOffsetAdjuster(1.47),
            FlaggedAdaptiveSwapWindowAdjuster(flags, "adaptive-swap-window-lenient", -2.8),
            TieredAdaptiveSwapWindowAdjuster(
                mutableListOf<Double>(1371.0, 1808.0, 2098.0, 8472.0),
                mutableListOf<Double>(0.89, 0.92, 1.04, 1.14),
            ),
            RoundingAdaptiveSwapWindowAdjuster(0.1),
            PercentAdaptiveSwapWindowAdjuster(20.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 68849.6,
    )
}
