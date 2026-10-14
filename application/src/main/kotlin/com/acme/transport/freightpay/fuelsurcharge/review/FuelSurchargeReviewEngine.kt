package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fuel surcharge review calculation. */
interface FuelSurchargeReviewFactor {
    fun describe(): String
    fun adjust(item: FuelSurchargeReview, value: Double): Double
}

data class PercentFuelSurchargeReviewAdjuster(var percent: Double) : FuelSurchargeReviewFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelSurchargeReview, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredFuelSurchargeReviewAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelSurchargeReviewFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelSurchargeReview, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapFuelSurchargeReviewAdjuster(var cap: Double) : FuelSurchargeReviewFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelSurchargeReview, value: Double): Double = minOf(value, cap)
}

data class MinimumFuelSurchargeReviewAdjuster(var minimum: Double) : FuelSurchargeReviewFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelSurchargeReview, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CountryFuelSurchargeReviewAdjuster(
    var factors: MutableMap<String, Double>,
) : FuelSurchargeReviewFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: FuelSurchargeReview, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fuel surcharge review steps in order and keeps a trace of each. */
data class FuelSurchargeReviewEngine(
    var steps: MutableList<FuelSurchargeReviewFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelSurchargeReview): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelSurchargeReview>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelSurchargeReviewEngine(): FuelSurchargeReviewEngine {
    return FuelSurchargeReviewEngine(
        steps = mutableListOf<FuelSurchargeReviewFactor>(
            PercentFuelSurchargeReviewAdjuster(22.2),
            TieredFuelSurchargeReviewAdjuster(
                mutableListOf<Double>(1995.0, 4395.0, 6016.0),
                mutableListOf<Double>(1.09, 1.03, 0.89),
            ),
            CapFuelSurchargeReviewAdjuster(31211.0),
            MinimumFuelSurchargeReviewAdjuster(13.8),
            CountryFuelSurchargeReviewAdjuster(mutableMapOf<String, Double>("west" to 1.18)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 91753.1,
    )
}
