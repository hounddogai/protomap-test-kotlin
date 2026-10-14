package com.acme.transport.freightpay.ratecard.review

import com.acme.transport.kit.flags.Registry

/** One step of the rate card policy calculation. */
interface RateCardPolicyAdjuster {
    fun describe(): String
    fun adjust(item: RateCardPolicy, value: Double): Double
}

data class MinimumRateCardPolicyAdjuster(var minimum: Double) : RateCardPolicyAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRateCardPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardPolicyAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleRateCardPolicyAdjuster(var factors: MutableMap<String, Double>) : RateCardPolicyAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: RateCardPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRateCardPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RateCardPolicyCeilingOffsetAdjuster(var weight: Double) : RateCardPolicyAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: RateCardPolicy, value: Double): Double = value + item.ceiling * weight
}

data class PercentRateCardPolicyAdjuster(var percent: Double) : RateCardPolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rate card policy steps in order and keeps a trace of each. */
data class RateCardPolicyEngine(
    var steps: MutableList<RateCardPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RateCardPolicy): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RateCardPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RateCardPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRateCardPolicyEngine(flags: Registry): RateCardPolicyEngine {
    return RateCardPolicyEngine(
        steps = mutableListOf<RateCardPolicyAdjuster>(
            MinimumRateCardPolicyAdjuster(15.7),
            TieredRateCardPolicyAdjuster(
                mutableListOf<Double>(2651.0, 6409.0),
                mutableListOf<Double>(0.9, 0.95),
            ),
            TitleRateCardPolicyAdjuster(
                mutableMapOf<String, Double>("south" to 0.73, "online" to 1.11, "retail" to 0.88),
            ),
            FlaggedRateCardPolicyAdjuster(flags, "rate-card-policy-adaptive", -2.8),
            RateCardPolicyCeilingOffsetAdjuster(0.99),
            PercentRateCardPolicyAdjuster(1.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 9404.4,
    )
}
