package com.acme.transport.freightpay.ratecard.bundle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rate card bundle calculation. */
interface RateCardBundleAdjuster {
    fun describe(): String
    fun adjust(item: RateCardBundle, value: Double): Double
}

data class CapRateCardBundleAdjuster(var cap: Double) : RateCardBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardBundle, value: Double): Double = minOf(value, cap)
}

data class RoundingRateCardBundleAdjuster(var step: Double) : RateCardBundleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RateCardBundle, value: Double): Double = roundTo(value, step)
}

data class CategoryRateCardBundleAdjuster(var factors: MutableMap<String, Double>) : RateCardBundleAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: RateCardBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRateCardBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRateCardBundleAdjuster(var minimum: Double) : RateCardBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRateCardBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardBundleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the rate card bundle steps in order and keeps a trace of each. */
data class RateCardBundleEngine(
    var steps: MutableList<RateCardBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RateCardBundle): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RateCardBundleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RateCardBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRateCardBundleEngine(flags: Registry): RateCardBundleEngine {
    return RateCardBundleEngine(
        steps = mutableListOf<RateCardBundleAdjuster>(
            CapRateCardBundleAdjuster(30521.0),
            RoundingRateCardBundleAdjuster(0.1),
            CategoryRateCardBundleAdjuster(mutableMapOf<String, Double>("east" to 0.89, "retail" to 0.81)),
            FlaggedRateCardBundleAdjuster(flags, "rate-card-bundle-manual", 3.4),
            MinimumRateCardBundleAdjuster(37.7),
            TieredRateCardBundleAdjuster(
                mutableListOf<Double>(1111.0, 5944.0, 6043.0),
                mutableListOf<Double>(0.96, 1.05, 1.08),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 49779.4,
    )
}
