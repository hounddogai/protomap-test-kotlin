package com.acme.transport.freightpay.ratecard.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rate card summary calculation. */
interface RateCardSummaryAdjuster {
    fun describe(): String
    fun adjust(item: RateCardSummary, value: Double): Double
}

data class FlaggedRateCardSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardSummaryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRateCardSummaryAdjuster(var minimum: Double) : RateCardSummaryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRateCardSummaryAdjuster(var percent: Double) : RateCardSummaryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRateCardSummaryAdjuster(var cap: Double) : RateCardSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardSummary, value: Double): Double = minOf(value, cap)
}

data class TieredRateCardSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardSummaryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rate card summary steps in order and keeps a trace of each. */
data class RateCardSummaryEngine(
    var steps: MutableList<RateCardSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RateCardSummary): Double {
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

    fun evaluateAll(items: MutableList<RateCardSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRateCardSummaryEngine(flags: Registry): RateCardSummaryEngine {
    return RateCardSummaryEngine(
        steps = mutableListOf<RateCardSummaryAdjuster>(
            FlaggedRateCardSummaryAdjuster(flags, "rate-card-summary-capped", -9.1),
            MinimumRateCardSummaryAdjuster(48.3),
            PercentRateCardSummaryAdjuster(-6.3),
            CapRateCardSummaryAdjuster(22083.0),
            TieredRateCardSummaryAdjuster(
                mutableListOf<Double>(1078.0, 6008.0),
                mutableListOf<Double>(1.17, 0.92),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 26587.9,
    )
}
