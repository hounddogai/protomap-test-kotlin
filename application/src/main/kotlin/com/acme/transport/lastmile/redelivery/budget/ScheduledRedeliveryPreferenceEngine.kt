package com.acme.transport.lastmile.redelivery.budget

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled redelivery preference calculation. */
interface ScheduledRedeliveryPreferenceFactor {
    fun describe(): String
    fun adjust(item: ScheduledRedeliveryPreference, value: Double): Double
}

data class FlaggedScheduledRedeliveryPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledRedeliveryPreferenceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledRedeliveryPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScheduledRedeliveryPreferenceAdjuster(
    var minimum: Double,
) : ScheduledRedeliveryPreferenceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledRedeliveryPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapScheduledRedeliveryPreferenceAdjuster(var cap: Double) : ScheduledRedeliveryPreferenceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledRedeliveryPreference, value: Double): Double = minOf(value, cap)
}

data class TieredScheduledRedeliveryPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledRedeliveryPreferenceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledRedeliveryPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled redelivery preference steps in order and keeps a trace of each. */
data class ScheduledRedeliveryPreferenceEngine(
    var steps: MutableList<ScheduledRedeliveryPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledRedeliveryPreference): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledRedeliveryPreferenceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledRedeliveryPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledRedeliveryPreferenceEngine(flags: Registry): ScheduledRedeliveryPreferenceEngine {
    return ScheduledRedeliveryPreferenceEngine(
        steps = mutableListOf<ScheduledRedeliveryPreferenceFactor>(
            FlaggedScheduledRedeliveryPreferenceAdjuster(flags, "scheduled-redelivery-preference-lenient", 1.8),
            MinimumScheduledRedeliveryPreferenceAdjuster(10.7),
            CapScheduledRedeliveryPreferenceAdjuster(27628.0),
            TieredScheduledRedeliveryPreferenceAdjuster(
                mutableListOf<Double>(1326.0, 4821.0, 5627.0, 9279.0),
                mutableListOf<Double>(1.02, 1.06, 1.08, 0.9),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 67822.6,
    )
}
