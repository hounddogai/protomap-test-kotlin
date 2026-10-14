package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rate card milestone calculation. */
interface RateCardMilestoneModifier {
    fun describe(): String
    fun adjust(item: RateCardMilestone, value: Double): Double
}

data class RegionRateCardMilestoneAdjuster(
    var factors: MutableMap<String, Double>,
) : RateCardMilestoneModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: RateCardMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRateCardMilestoneAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardMilestoneModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardMilestone, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRateCardMilestoneAdjuster(var step: Double) : RateCardMilestoneModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RateCardMilestone, value: Double): Double = roundTo(value, step)
}

data class PercentRateCardMilestoneAdjuster(var percent: Double) : RateCardMilestoneModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardMilestone, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RateCardMilestoneStartsAtOffsetAdjuster(var weight: Double) : RateCardMilestoneModifier {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: RateCardMilestone, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class FlaggedRateCardMilestoneAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardMilestoneModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardMilestone, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rate card milestone steps in order and keeps a trace of each. */
data class RateCardMilestoneEngine(
    var steps: MutableList<RateCardMilestoneModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RateCardMilestone): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RateCardMilestoneModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RateCardMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRateCardMilestoneEngine(flags: Registry): RateCardMilestoneEngine {
    return RateCardMilestoneEngine(
        steps = mutableListOf<RateCardMilestoneModifier>(
            RegionRateCardMilestoneAdjuster(mutableMapOf<String, Double>("east" to 1.39, "west" to 0.96)),
            TieredRateCardMilestoneAdjuster(
                mutableListOf<Double>(1946.0, 2967.0),
                mutableListOf<Double>(0.94, 1.2),
            ),
            RoundingRateCardMilestoneAdjuster(0.25),
            PercentRateCardMilestoneAdjuster(21.9),
            RateCardMilestoneStartsAtOffsetAdjuster(1.39),
            FlaggedRateCardMilestoneAdjuster(flags, "rate-card-milestone-legacy", -2.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 8532.4,
    )
}
