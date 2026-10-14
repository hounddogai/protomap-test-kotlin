package com.acme.transport.freightpay.ratecard.adjustment

/** One step of the rate card override calculation. */
interface RateCardOverrideAdjuster {
    fun describe(): String
    fun adjust(item: RateCardOverride, value: Double): Double
}

data class RateCardOverrideFuelLitersOffsetAdjuster(var weight: Double) : RateCardOverrideAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: RateCardOverride, value: Double): Double = value + item.fuelLiters * weight
}

data class TieredRateCardOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardOverrideAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRateCardOverrideAdjuster(var minimum: Double) : RateCardOverrideAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rate card override steps in order and keeps a trace of each. */
data class RateCardOverrideEngine(
    var steps: MutableList<RateCardOverrideAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RateCardOverride): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RateCardOverrideAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RateCardOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRateCardOverrideEngine(): RateCardOverrideEngine {
    return RateCardOverrideEngine(
        steps = mutableListOf<RateCardOverrideAdjuster>(
            RateCardOverrideFuelLitersOffsetAdjuster(0.49),
            TieredRateCardOverrideAdjuster(
                mutableListOf<Double>(4341.0, 7928.0),
                mutableListOf<Double>(1.14, 1.17),
            ),
            MinimumRateCardOverrideAdjuster(12.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 93467.9,
    )
}
