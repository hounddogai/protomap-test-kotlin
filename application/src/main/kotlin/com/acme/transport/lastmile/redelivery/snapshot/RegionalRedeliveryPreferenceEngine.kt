package com.acme.transport.lastmile.redelivery.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional redelivery preference calculation. */
interface RegionalRedeliveryPreferenceModifier {
    fun describe(): String
    fun adjust(item: RegionalRedeliveryPreference, value: Double): Double
}

data class TieredRegionalRedeliveryPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalRedeliveryPreferenceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalRedeliveryPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRegionalRedeliveryPreferenceAdjuster(
    var minimum: Double,
) : RegionalRedeliveryPreferenceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalRedeliveryPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RegionalRedeliveryPreferenceLimitOffsetAdjuster(
    var weight: Double,
) : RegionalRedeliveryPreferenceModifier {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: RegionalRedeliveryPreference, value: Double): Double {
        return value + item.limit.toDouble() * weight
    }
}

data class FlaggedRegionalRedeliveryPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalRedeliveryPreferenceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalRedeliveryPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRegionalRedeliveryPreferenceAdjuster(
    var step: Double,
) : RegionalRedeliveryPreferenceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalRedeliveryPreference, value: Double): Double = roundTo(value, step)
}

/** Runs the regional redelivery preference steps in order and keeps a trace of each. */
data class RegionalRedeliveryPreferenceEngine(
    var steps: MutableList<RegionalRedeliveryPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalRedeliveryPreference): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalRedeliveryPreferenceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalRedeliveryPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalRedeliveryPreferenceEngine(flags: Registry): RegionalRedeliveryPreferenceEngine {
    return RegionalRedeliveryPreferenceEngine(
        steps = mutableListOf<RegionalRedeliveryPreferenceModifier>(
            TieredRegionalRedeliveryPreferenceAdjuster(
                mutableListOf<Double>(2722.0, 9643.0),
                mutableListOf<Double>(0.98, 1.08),
            ),
            MinimumRegionalRedeliveryPreferenceAdjuster(15.2),
            RegionalRedeliveryPreferenceLimitOffsetAdjuster(1.83),
            FlaggedRegionalRedeliveryPreferenceAdjuster(flags, "regional-redelivery-preference-tiered", -5.6),
            RoundingRegionalRedeliveryPreferenceAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 60952.8,
    )
}
