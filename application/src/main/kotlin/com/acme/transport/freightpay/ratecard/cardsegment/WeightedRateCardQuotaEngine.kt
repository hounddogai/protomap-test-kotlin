package com.acme.transport.freightpay.ratecard.cardsegment

/** One step of the weighted rate card quota calculation. */
interface WeightedRateCardQuotaFactor {
    fun describe(): String
    fun adjust(item: WeightedRateCardQuota, value: Double): Double
}

data class CapWeightedRateCardQuotaAdjuster(var cap: Double) : WeightedRateCardQuotaFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedRateCardQuota, value: Double): Double = minOf(value, cap)
}

data class MinimumWeightedRateCardQuotaAdjuster(var minimum: Double) : WeightedRateCardQuotaFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedRateCardQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentWeightedRateCardQuotaAdjuster(var percent: Double) : WeightedRateCardQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedRateCardQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredWeightedRateCardQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedRateCardQuotaFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedRateCardQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted rate card quota steps in order and keeps a trace of each. */
data class WeightedRateCardQuotaEngine(
    var steps: MutableList<WeightedRateCardQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: WeightedRateCardQuota): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedRateCardQuotaFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedRateCardQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultWeightedRateCardQuotaEngine(): WeightedRateCardQuotaEngine {
    return WeightedRateCardQuotaEngine(
        steps = mutableListOf<WeightedRateCardQuotaFactor>(
            CapWeightedRateCardQuotaAdjuster(32981.0),
            MinimumWeightedRateCardQuotaAdjuster(31.1),
            PercentWeightedRateCardQuotaAdjuster(9.1),
            TieredWeightedRateCardQuotaAdjuster(
                mutableListOf<Double>(1044.0, 4218.0, 8894.0, 9024.0),
                mutableListOf<Double>(1.13, 0.93, 1.11, 0.93),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 52139.1,
    )
}
