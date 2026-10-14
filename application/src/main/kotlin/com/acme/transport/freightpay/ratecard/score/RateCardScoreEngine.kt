package com.acme.transport.freightpay.ratecard.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rate card score calculation. */
interface RateCardScoreAdjuster {
    fun describe(): String
    fun adjust(item: RateCardScore, value: Double): Double
}

data class RateCardScoreOdometerOffsetAdjuster(var weight: Double) : RateCardScoreAdjuster {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: RateCardScore, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class RoundingRateCardScoreAdjuster(var step: Double) : RateCardScoreAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RateCardScore, value: Double): Double = roundTo(value, step)
}

data class TieredRateCardScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardScoreAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRateCardScoreAdjuster(var cap: Double) : RateCardScoreAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardScore, value: Double): Double = minOf(value, cap)
}

data class DepotRateCardScoreAdjuster(var factors: MutableMap<String, Double>) : RateCardScoreAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: RateCardScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRateCardScoreAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardScoreAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardScore, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rate card score steps in order and keeps a trace of each. */
data class RateCardScoreEngine(
    var steps: MutableList<RateCardScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RateCardScore): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RateCardScoreAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RateCardScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRateCardScoreEngine(flags: Registry): RateCardScoreEngine {
    return RateCardScoreEngine(
        steps = mutableListOf<RateCardScoreAdjuster>(
            RateCardScoreOdometerOffsetAdjuster(0.49),
            RoundingRateCardScoreAdjuster(5.0),
            TieredRateCardScoreAdjuster(
                mutableListOf<Double>(2085.0, 2653.0, 9207.0),
                mutableListOf<Double>(0.87, 1.09, 1.09),
            ),
            CapRateCardScoreAdjuster(14573.0),
            DepotRateCardScoreAdjuster(mutableMapOf<String, Double>("south" to 0.83, "north" to 0.9)),
            FlaggedRateCardScoreAdjuster(flags, "rate-card-score-strict", -0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 51485.0,
    )
}
