package com.acme.transport.freightpay.ratecard.ledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rate card metric calculation. */
interface RateCardMetricStep {
    fun describe(): String
    fun adjust(item: RateCardMetric, value: Double): Double
}

data class RoundingRateCardMetricAdjuster(var step: Double) : RateCardMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RateCardMetric, value: Double): Double = roundTo(value, step)
}

data class CapRateCardMetricAdjuster(var cap: Double) : RateCardMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardMetric, value: Double): Double = minOf(value, cap)
}

data class MinimumRateCardMetricAdjuster(var minimum: Double) : RateCardMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRateCardMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardMetricStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRateCardMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RateCardMetricEtaMinutesOffsetAdjuster(var weight: Double) : RateCardMetricStep {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: RateCardMetric, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

/** Runs the rate card metric steps in order and keeps a trace of each. */
data class RateCardMetricEngine(
    var steps: MutableList<RateCardMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RateCardMetric): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RateCardMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<RateCardMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRateCardMetricEngine(flags: Registry): RateCardMetricEngine {
    return RateCardMetricEngine(
        steps = mutableListOf<RateCardMetricStep>(
            RoundingRateCardMetricAdjuster(5.0),
            CapRateCardMetricAdjuster(3319.0),
            MinimumRateCardMetricAdjuster(0.8),
            FlaggedRateCardMetricAdjuster(flags, "rate-card-metric-default", 9.3),
            TieredRateCardMetricAdjuster(
                mutableListOf<Double>(6194.0, 6608.0, 7421.0),
                mutableListOf<Double>(1.09, 1.09, 1.16),
            ),
            RateCardMetricEtaMinutesOffsetAdjuster(0.84),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 30062.8,
    )
}
