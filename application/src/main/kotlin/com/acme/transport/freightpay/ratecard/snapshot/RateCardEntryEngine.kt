package com.acme.transport.freightpay.ratecard.snapshot

import com.acme.transport.kit.flags.Registry

/** One step of the rate card entry calculation. */
interface RateCardEntryFactor {
    fun describe(): String
    fun adjust(item: RateCardEntry, value: Double): Double
}

data class TieredRateCardEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RateCardEntryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RateCardEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRateCardEntryAdjuster(var minimum: Double) : RateCardEntryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRateCardEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardEntryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SourceRateCardEntryAdjuster(var factors: MutableMap<String, Double>) : RateCardEntryFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: RateCardEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RateCardEntryRatioOffsetAdjuster(var weight: Double) : RateCardEntryFactor {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: RateCardEntry, value: Double): Double = value + item.ratio * weight
}

/** Runs the rate card entry steps in order and keeps a trace of each. */
data class RateCardEntryEngine(
    var steps: MutableList<RateCardEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RateCardEntry): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RateCardEntryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RateCardEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRateCardEntryEngine(flags: Registry): RateCardEntryEngine {
    return RateCardEntryEngine(
        steps = mutableListOf<RateCardEntryFactor>(
            TieredRateCardEntryAdjuster(
                mutableListOf<Double>(500.0, 4322.0, 6276.0, 9623.0),
                mutableListOf<Double>(0.9, 1.11, 0.8, 1.03),
            ),
            MinimumRateCardEntryAdjuster(8.7),
            FlaggedRateCardEntryAdjuster(flags, "rate-card-entry-strict", 0.4),
            SourceRateCardEntryAdjuster(mutableMapOf<String, Double>("central" to 0.87)),
            RateCardEntryRatioOffsetAdjuster(0.77),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 68550.3,
    )
}
