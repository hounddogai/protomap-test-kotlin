package com.acme.transport.crewing.restbreak.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rest break tier calculation. */
interface RestBreakTierModifier {
    fun describe(): String
    fun adjust(item: RestBreakTier, value: Double): Double
}

data class RoundingRestBreakTierAdjuster(var step: Double) : RestBreakTierModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakTier, value: Double): Double = roundTo(value, step)
}

data class ChannelRestBreakTierAdjuster(var factors: MutableMap<String, Double>) : RestBreakTierModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: RestBreakTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRestBreakTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakTierModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRestBreakTierAdjuster(var percent: Double) : RestBreakTierModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RestBreakTierScoreOffsetAdjuster(var weight: Double) : RestBreakTierModifier {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: RestBreakTier, value: Double): Double = value + item.score * weight
}

data class MinimumRestBreakTierAdjuster(var minimum: Double) : RestBreakTierModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rest break tier steps in order and keeps a trace of each. */
data class RestBreakTierEngine(
    var steps: MutableList<RestBreakTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RestBreakTier): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakTierModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRestBreakTierEngine(flags: Registry): RestBreakTierEngine {
    return RestBreakTierEngine(
        steps = mutableListOf<RestBreakTierModifier>(
            RoundingRestBreakTierAdjuster(5.0),
            ChannelRestBreakTierAdjuster(mutableMapOf<String, Double>("east" to 0.77)),
            FlaggedRestBreakTierAdjuster(flags, "rest-break-tier-capped", 3.7),
            PercentRestBreakTierAdjuster(22.3),
            RestBreakTierScoreOffsetAdjuster(0.06),
            MinimumRestBreakTierAdjuster(23.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 36128.2,
    )
}
