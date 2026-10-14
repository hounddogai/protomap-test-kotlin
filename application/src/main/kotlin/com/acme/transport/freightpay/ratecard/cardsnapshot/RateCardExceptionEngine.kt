package com.acme.transport.freightpay.ratecard.cardsnapshot

import com.acme.transport.kit.flags.Registry

/** One step of the rate card exception calculation. */
interface RateCardExceptionAdjuster {
    fun describe(): String
    fun adjust(item: RateCardException, value: Double): Double
}

data class FlaggedRateCardExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionRateCardExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : RateCardExceptionAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: RateCardException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRateCardExceptionAdjuster(var cap: Double) : RateCardExceptionAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardException, value: Double): Double = minOf(value, cap)
}

data class PercentRateCardExceptionAdjuster(var percent: Double) : RateCardExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RateCardException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRateCardExceptionAdjuster(var minimum: Double) : RateCardExceptionAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rate card exception steps in order and keeps a trace of each. */
data class RateCardExceptionEngine(
    var steps: MutableList<RateCardExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RateCardException): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RateCardExceptionAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RateCardException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRateCardExceptionEngine(flags: Registry): RateCardExceptionEngine {
    return RateCardExceptionEngine(
        steps = mutableListOf<RateCardExceptionAdjuster>(
            FlaggedRateCardExceptionAdjuster(flags, "rate-card-exception-primary", 6.0),
            RegionRateCardExceptionAdjuster(mutableMapOf<String, Double>("central" to 0.87, "south" to 0.81)),
            CapRateCardExceptionAdjuster(30545.0),
            PercentRateCardExceptionAdjuster(-24.0),
            MinimumRateCardExceptionAdjuster(19.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 69283.6,
    )
}
