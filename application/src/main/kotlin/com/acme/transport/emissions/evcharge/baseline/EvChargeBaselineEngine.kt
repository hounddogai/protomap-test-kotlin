package com.acme.transport.emissions.evcharge.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the ev charge baseline calculation. */
interface EvChargeBaselineStep {
    fun describe(): String
    fun adjust(item: EvChargeBaseline, value: Double): Double
}

data class EvChargeBaselineAmountOffsetAdjuster(var weight: Double) : EvChargeBaselineStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: EvChargeBaseline, value: Double): Double = value + item.amount * weight
}

data class MinimumEvChargeBaselineAdjuster(var minimum: Double) : EvChargeBaselineStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedEvChargeBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeBaselineStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelEvChargeBaselineAdjuster(var factors: MutableMap<String, Double>) : EvChargeBaselineStep {
    override fun describe(): String = "by label"

    override fun adjust(item: EvChargeBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingEvChargeBaselineAdjuster(var step: Double) : EvChargeBaselineStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeBaseline, value: Double): Double = roundTo(value, step)
}

/** Runs the ev charge baseline steps in order and keeps a trace of each. */
data class EvChargeBaselineEngine(
    var steps: MutableList<EvChargeBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EvChargeBaseline): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeBaselineStep = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEvChargeBaselineEngine(flags: Registry): EvChargeBaselineEngine {
    return EvChargeBaselineEngine(
        steps = mutableListOf<EvChargeBaselineStep>(
            EvChargeBaselineAmountOffsetAdjuster(0.46),
            MinimumEvChargeBaselineAdjuster(4.3),
            FlaggedEvChargeBaselineAdjuster(flags, "ev-charge-baseline-manual", -3.1),
            LabelEvChargeBaselineAdjuster(mutableMapOf<String, Double>("north" to 0.75)),
            RoundingEvChargeBaselineAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 45596.0,
    )
}
