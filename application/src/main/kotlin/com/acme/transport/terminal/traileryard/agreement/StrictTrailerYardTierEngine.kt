package com.acme.transport.terminal.traileryard.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict trailer yard tier calculation. */
interface StrictTrailerYardTierStep {
    fun describe(): String
    fun adjust(item: StrictTrailerYardTier, value: Double): Double
}

data class MinimumStrictTrailerYardTierAdjuster(var minimum: Double) : StrictTrailerYardTierStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictTrailerYardTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingStrictTrailerYardTierAdjuster(var step: Double) : StrictTrailerYardTierStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictTrailerYardTier, value: Double): Double = roundTo(value, step)
}

data class PercentStrictTrailerYardTierAdjuster(var percent: Double) : StrictTrailerYardTierStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictTrailerYardTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedStrictTrailerYardTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictTrailerYardTierStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictTrailerYardTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class StrictTrailerYardTierAmountOffsetAdjuster(var weight: Double) : StrictTrailerYardTierStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: StrictTrailerYardTier, value: Double): Double = value + item.amount * weight
}

/** Runs the strict trailer yard tier steps in order and keeps a trace of each. */
data class StrictTrailerYardTierEngine(
    var steps: MutableList<StrictTrailerYardTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictTrailerYardTier): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictTrailerYardTierStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictTrailerYardTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictTrailerYardTierEngine(flags: Registry): StrictTrailerYardTierEngine {
    return StrictTrailerYardTierEngine(
        steps = mutableListOf<StrictTrailerYardTierStep>(
            MinimumStrictTrailerYardTierAdjuster(22.2),
            RoundingStrictTrailerYardTierAdjuster(0.05),
            PercentStrictTrailerYardTierAdjuster(8.3),
            FlaggedStrictTrailerYardTierAdjuster(flags, "strict-trailer-yard-tier-rolling", -7.4),
            StrictTrailerYardTierAmountOffsetAdjuster(0.75),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 9116.6,
    )
}
