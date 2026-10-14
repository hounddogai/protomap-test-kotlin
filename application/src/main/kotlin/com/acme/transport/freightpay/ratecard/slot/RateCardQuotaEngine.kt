package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rate card quota calculation. */
interface RateCardQuotaFactor {
    fun describe(): String
    fun adjust(item: RateCardQuota, value: Double): Double
}

data class TieredRateCardQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardQuotaFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRateCardQuotaAdjuster(var step: Double) : RateCardQuotaFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RateCardQuota, value: Double): Double = roundTo(value, step)
}

data class FlaggedRateCardQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardQuotaFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRateCardQuotaAdjuster(var percent: Double) : RateCardQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rate card quota steps in order and keeps a trace of each. */
data class RateCardQuotaEngine(
    var steps: MutableList<RateCardQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RateCardQuota): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RateCardQuotaFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RateCardQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRateCardQuotaEngine(flags: Registry): RateCardQuotaEngine {
    return RateCardQuotaEngine(
        steps = mutableListOf<RateCardQuotaFactor>(
            TieredRateCardQuotaAdjuster(
                mutableListOf<Double>(1562.0, 2989.0, 3065.0, 9696.0),
                mutableListOf<Double>(1.13, 1.16, 1.1, 1.01),
            ),
            RoundingRateCardQuotaAdjuster(0.5),
            FlaggedRateCardQuotaAdjuster(flags, "rate-card-quota-adaptive", 1.1),
            PercentRateCardQuotaAdjuster(-5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 2346.9,
    )
}
