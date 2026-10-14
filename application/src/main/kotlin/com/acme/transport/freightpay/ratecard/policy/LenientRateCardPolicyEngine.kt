package com.acme.transport.freightpay.ratecard.policy

/** One step of the lenient rate card policy calculation. */
interface LenientRateCardPolicyStep {
    fun describe(): String
    fun adjust(item: LenientRateCardPolicy, value: Double): Double
}

data class MinimumLenientRateCardPolicyAdjuster(var minimum: Double) : LenientRateCardPolicyStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientRateCardPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LenientRateCardPolicyRevisionOffsetAdjuster(var weight: Double) : LenientRateCardPolicyStep {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: LenientRateCardPolicy, value: Double): Double {
        return value + item.revision.toDouble() * weight
    }
}

data class TieredLenientRateCardPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientRateCardPolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientRateCardPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLenientRateCardPolicyAdjuster(var percent: Double) : LenientRateCardPolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientRateCardPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ChannelLenientRateCardPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientRateCardPolicyStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: LenientRateCardPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient rate card policy steps in order and keeps a trace of each. */
data class LenientRateCardPolicyEngine(
    var steps: MutableList<LenientRateCardPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientRateCardPolicy): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientRateCardPolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientRateCardPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientRateCardPolicyEngine(): LenientRateCardPolicyEngine {
    return LenientRateCardPolicyEngine(
        steps = mutableListOf<LenientRateCardPolicyStep>(
            MinimumLenientRateCardPolicyAdjuster(26.7),
            LenientRateCardPolicyRevisionOffsetAdjuster(1.39),
            TieredLenientRateCardPolicyAdjuster(
                mutableListOf<Double>(1768.0, 3750.0, 4311.0, 6836.0),
                mutableListOf<Double>(1.03, 0.89, 1.18, 0.94),
            ),
            PercentLenientRateCardPolicyAdjuster(5.7),
            ChannelLenientRateCardPolicyAdjuster(mutableMapOf<String, Double>("east" to 1.16)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 82980.7,
    )
}
