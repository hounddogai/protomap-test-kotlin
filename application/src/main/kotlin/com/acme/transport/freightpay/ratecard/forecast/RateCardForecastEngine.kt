package com.acme.transport.freightpay.ratecard.forecast

/** One step of the rate card forecast calculation. */
interface RateCardForecastModifier {
    fun describe(): String
    fun adjust(item: RateCardForecast, value: Double): Double
}

data class TieredRateCardForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardForecastModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRateCardForecastAdjuster(var percent: Double) : RateCardForecastModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardForecast, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneRateCardForecastAdjuster(var factors: MutableMap<String, Double>) : RateCardForecastModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: RateCardForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRateCardForecastAdjuster(var cap: Double) : RateCardForecastModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardForecast, value: Double): Double = minOf(value, cap)
}

/** Runs the rate card forecast steps in order and keeps a trace of each. */
data class RateCardForecastEngine(
    var steps: MutableList<RateCardForecastModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RateCardForecast): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RateCardForecastModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RateCardForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRateCardForecastEngine(): RateCardForecastEngine {
    return RateCardForecastEngine(
        steps = mutableListOf<RateCardForecastModifier>(
            TieredRateCardForecastAdjuster(
                mutableListOf<Double>(4126.0, 8142.0),
                mutableListOf<Double>(0.98, 0.98),
            ),
            PercentRateCardForecastAdjuster(15.9),
            LaneRateCardForecastAdjuster(mutableMapOf<String, Double>("west" to 1.18, "east" to 1.05)),
            CapRateCardForecastAdjuster(20350.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 89995.6,
    )
}
