package com.acme.transport.freightpay.ratecard.cardrequest

/** One step of the tiered rate card request calculation. */
interface TieredRateCardRequestAdjuster {
    fun describe(): String
    fun adjust(item: TieredRateCardRequest, value: Double): Double
}

data class PercentTieredRateCardRequestAdjuster(var percent: Double) : TieredRateCardRequestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredRateCardRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SourceTieredRateCardRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredRateCardRequestAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: TieredRateCardRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTieredRateCardRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredRateCardRequestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredRateCardRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered rate card request steps in order and keeps a trace of each. */
data class TieredRateCardRequestEngine(
    var steps: MutableList<TieredRateCardRequestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredRateCardRequest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredRateCardRequestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TieredRateCardRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredRateCardRequestEngine(): TieredRateCardRequestEngine {
    return TieredRateCardRequestEngine(
        steps = mutableListOf<TieredRateCardRequestAdjuster>(
            PercentTieredRateCardRequestAdjuster(11.0),
            SourceTieredRateCardRequestAdjuster(mutableMapOf<String, Double>("online" to 1.07)),
            TieredTieredRateCardRequestAdjuster(
                mutableListOf<Double>(1542.0, 8484.0),
                mutableListOf<Double>(0.91, 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 52866.4,
    )
}
