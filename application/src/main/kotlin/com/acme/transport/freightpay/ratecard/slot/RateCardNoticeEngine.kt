package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rate card notice calculation. */
interface RateCardNoticeModifier {
    fun describe(): String
    fun adjust(item: RateCardNotice, value: Double): Double
}

data class MinimumRateCardNoticeAdjuster(var minimum: Double) : RateCardNoticeModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ChannelRateCardNoticeAdjuster(var factors: MutableMap<String, Double>) : RateCardNoticeModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: RateCardNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRateCardNoticeAdjuster(var percent: Double) : RateCardNoticeModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRateCardNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardNoticeModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rate card notice steps in order and keeps a trace of each. */
data class RateCardNoticeEngine(
    var steps: MutableList<RateCardNoticeModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RateCardNotice): Double {
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

    fun evaluateAll(items: MutableList<RateCardNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRateCardNoticeEngine(): RateCardNoticeEngine {
    return RateCardNoticeEngine(
        steps = mutableListOf<RateCardNoticeModifier>(
            MinimumRateCardNoticeAdjuster(11.4),
            ChannelRateCardNoticeAdjuster(mutableMapOf<String, Double>("south" to 0.99, "west" to 1.0)),
            PercentRateCardNoticeAdjuster(-22.7),
            TieredRateCardNoticeAdjuster(
                mutableListOf<Double>(2054.0, 4489.0, 5390.0, 6917.0),
                mutableListOf<Double>(1.15, 0.82, 0.81, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 24808.0,
    )
}
