package com.acme.transport.safety.coaching.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the coaching tier calculation. */
interface CoachingTierStep {
    fun describe(): String
    fun adjust(item: CoachingTier, value: Double): Double
}

data class LocaleCoachingTierAdjuster(var factors: MutableMap<String, Double>) : CoachingTierStep {
    override fun describe(): String = "by locale"

    override fun adjust(item: CoachingTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCoachingTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CoachingTierStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CoachingTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCoachingTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingTierStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCoachingTierAdjuster(var minimum: Double) : CoachingTierStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CoachingTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCoachingTierAdjuster(var step: Double) : CoachingTierStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingTier, value: Double): Double = roundTo(value, step)
}

data class PercentCoachingTierAdjuster(var percent: Double) : CoachingTierStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the coaching tier steps in order and keeps a trace of each. */
data class CoachingTierEngine(
    var steps: MutableList<CoachingTierStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CoachingTier): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CoachingTierStep = steps[index]
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

    fun evaluateAll(items: MutableList<CoachingTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCoachingTierEngine(flags: Registry): CoachingTierEngine {
    return CoachingTierEngine(
        steps = mutableListOf<CoachingTierStep>(
            LocaleCoachingTierAdjuster(
                mutableMapOf<String, Double>("west" to 1.18, "north" to 0.97, "central" to 1.33),
            ),
            TieredCoachingTierAdjuster(
                mutableListOf<Double>(1205.0, 3346.0, 9323.0),
                mutableListOf<Double>(1.07, 1.0, 0.91),
            ),
            FlaggedCoachingTierAdjuster(flags, "coaching-tier-primary", -4.2),
            MinimumCoachingTierAdjuster(30.2),
            RoundingCoachingTierAdjuster(5.0),
            PercentCoachingTierAdjuster(20.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 6671.9,
    )
}
