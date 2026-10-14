package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rate card request calculation. */
interface RateCardRequestAdjuster {
    fun describe(): String
    fun adjust(item: RateCardRequest, value: Double): Double
}

data class MinimumRateCardRequestAdjuster(var minimum: Double) : RateCardRequestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RateCardRequestDiscountOffsetAdjuster(var weight: Double) : RateCardRequestAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: RateCardRequest, value: Double): Double = value + item.discount * weight
}

data class TieredRateCardRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardRequestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleRateCardRequestAdjuster(var factors: MutableMap<String, Double>) : RateCardRequestAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: RateCardRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rate card request steps in order and keeps a trace of each. */
data class RateCardRequestEngine(
    var steps: MutableList<RateCardRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RateCardRequest): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RateCardRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRateCardRequestEngine(): RateCardRequestEngine {
    return RateCardRequestEngine(
        steps = mutableListOf<RateCardRequestAdjuster>(
            MinimumRateCardRequestAdjuster(6.8),
            RateCardRequestDiscountOffsetAdjuster(1.7),
            TieredRateCardRequestAdjuster(
                mutableListOf<Double>(1591.0, 3397.0, 4988.0, 6368.0),
                mutableListOf<Double>(0.94, 0.86, 1.18, 1.05),
            ),
            TitleRateCardRequestAdjuster(mutableMapOf<String, Double>("north" to 1.16)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 35636.5,
    )
}
