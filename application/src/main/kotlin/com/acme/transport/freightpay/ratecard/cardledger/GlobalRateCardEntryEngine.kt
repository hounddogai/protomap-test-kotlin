package com.acme.transport.freightpay.ratecard.cardledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global rate card entry calculation. */
interface GlobalRateCardEntryAdjuster {
    fun describe(): String
    fun adjust(item: GlobalRateCardEntry, value: Double): Double
}

data class FlaggedGlobalRateCardEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalRateCardEntryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalRateCardEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalRateCardEntryAdjuster(var step: Double) : GlobalRateCardEntryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalRateCardEntry, value: Double): Double = roundTo(value, step)
}

data class PercentGlobalRateCardEntryAdjuster(var percent: Double) : GlobalRateCardEntryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalRateCardEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredGlobalRateCardEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalRateCardEntryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalRateCardEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameGlobalRateCardEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalRateCardEntryAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalRateCardEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumGlobalRateCardEntryAdjuster(var minimum: Double) : GlobalRateCardEntryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalRateCardEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the global rate card entry steps in order and keeps a trace of each. */
data class GlobalRateCardEntryEngine(
    var steps: MutableList<GlobalRateCardEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalRateCardEntry): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalRateCardEntryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalRateCardEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalRateCardEntryEngine(flags: Registry): GlobalRateCardEntryEngine {
    return GlobalRateCardEntryEngine(
        steps = mutableListOf<GlobalRateCardEntryAdjuster>(
            FlaggedGlobalRateCardEntryAdjuster(flags, "global-rate-card-entry-tiered", -4.8),
            RoundingGlobalRateCardEntryAdjuster(0.25),
            PercentGlobalRateCardEntryAdjuster(-7.1),
            TieredGlobalRateCardEntryAdjuster(
                mutableListOf<Double>(2162.0, 3549.0, 3752.0),
                mutableListOf<Double>(1.06, 0.94, 1.17),
            ),
            NameGlobalRateCardEntryAdjuster(mutableMapOf<String, Double>("north" to 0.74, "online" to 0.9)),
            MinimumGlobalRateCardEntryAdjuster(47.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 65904.4,
    )
}
