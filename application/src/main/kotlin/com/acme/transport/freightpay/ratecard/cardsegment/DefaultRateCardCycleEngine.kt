package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.kit.flags.Registry

/** One step of the default rate card cycle calculation. */
interface DefaultRateCardCycleAdjuster {
    fun describe(): String
    fun adjust(item: DefaultRateCardCycle, value: Double): Double
}

data class MinimumDefaultRateCardCycleAdjuster(var minimum: Double) : DefaultRateCardCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultRateCardCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDefaultRateCardCycleAdjuster(var percent: Double) : DefaultRateCardCycleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultRateCardCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDefaultRateCardCycleAdjuster(var cap: Double) : DefaultRateCardCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultRateCardCycle, value: Double): Double = minOf(value, cap)
}

data class TieredDefaultRateCardCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultRateCardCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultRateCardCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDefaultRateCardCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultRateCardCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultRateCardCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the default rate card cycle steps in order and keeps a trace of each. */
data class DefaultRateCardCycleEngine(
    var steps: MutableList<DefaultRateCardCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultRateCardCycle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultRateCardCycleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultRateCardCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultRateCardCycleEngine(flags: Registry): DefaultRateCardCycleEngine {
    return DefaultRateCardCycleEngine(
        steps = mutableListOf<DefaultRateCardCycleAdjuster>(
            MinimumDefaultRateCardCycleAdjuster(13.8),
            PercentDefaultRateCardCycleAdjuster(24.8),
            CapDefaultRateCardCycleAdjuster(10344.0),
            TieredDefaultRateCardCycleAdjuster(
                mutableListOf<Double>(2068.0, 8245.0),
                mutableListOf<Double>(1.07, 1.03),
            ),
            FlaggedDefaultRateCardCycleAdjuster(flags, "default-rate-card-cycle-primary", 3.9),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 57037.4,
    )
}
