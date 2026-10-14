package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rate card tier calculation. */
interface RateCardTierModifier {
    fun describe(): String
    fun adjust(item: RateCardTier, value: Double): Double
}

data class FlaggedRateCardTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardTierModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRateCardTierAdjuster(var step: Double) : RateCardTierModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RateCardTier, value: Double): Double = roundTo(value, step)
}

data class PercentRateCardTierAdjuster(var percent: Double) : RateCardTierModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRateCardTierAdjuster(var cap: Double) : RateCardTierModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardTier, value: Double): Double = minOf(value, cap)
}

data class TieredRateCardTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardTierModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rate card tier steps in order and keeps a trace of each. */
data class RateCardTierEngine(
    var steps: MutableList<RateCardTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RateCardTier): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RateCardTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRateCardTierEngine(flags: Registry): RateCardTierEngine {
    return RateCardTierEngine(
        steps = mutableListOf<RateCardTierModifier>(
            FlaggedRateCardTierAdjuster(flags, "rate-card-tier-default", -6.4),
            RoundingRateCardTierAdjuster(0.5),
            PercentRateCardTierAdjuster(16.9),
            CapRateCardTierAdjuster(33639.0),
            TieredRateCardTierAdjuster(
                mutableListOf<Double>(3096.0, 8369.0, 9927.0),
                mutableListOf<Double>(0.95, 0.89, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 94892.5,
    )
}
