package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the default swap override calculation. */
interface DefaultSwapOverrideStep {
    fun describe(): String
    fun adjust(item: DefaultSwapOverride, value: Double): Double
}

data class DefaultSwapOverrideGraceDaysOffsetAdjuster(var weight: Double) : DefaultSwapOverrideStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: DefaultSwapOverride, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class PercentDefaultSwapOverrideAdjuster(var percent: Double) : DefaultSwapOverrideStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultSwapOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDefaultSwapOverrideAdjuster(var minimum: Double) : DefaultSwapOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultSwapOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDefaultSwapOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultSwapOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultSwapOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingDefaultSwapOverrideAdjuster(var step: Double) : DefaultSwapOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultSwapOverride, value: Double): Double = roundTo(value, step)
}

/** Runs the default swap override steps in order and keeps a trace of each. */
data class DefaultSwapOverrideEngine(
    var steps: MutableList<DefaultSwapOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultSwapOverride): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultSwapOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultSwapOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultSwapOverrideEngine(flags: Registry): DefaultSwapOverrideEngine {
    return DefaultSwapOverrideEngine(
        steps = mutableListOf<DefaultSwapOverrideStep>(
            DefaultSwapOverrideGraceDaysOffsetAdjuster(1.21),
            PercentDefaultSwapOverrideAdjuster(24.9),
            MinimumDefaultSwapOverrideAdjuster(15.4),
            FlaggedDefaultSwapOverrideAdjuster(flags, "default-swap-override-scheduled", -5.7),
            RoundingDefaultSwapOverrideAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 95563.3,
    )
}
