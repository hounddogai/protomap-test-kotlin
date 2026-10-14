package com.acme.transport.lastmile.redelivery.baseline

import com.acme.transport.kit.flags.Registry

/** One step of the regional redelivery schedule calculation. */
interface RegionalRedeliveryScheduleModifier {
    fun describe(): String
    fun adjust(item: RegionalRedeliverySchedule, value: Double): Double
}

data class PercentRegionalRedeliveryScheduleAdjuster(var percent: Double) : RegionalRedeliveryScheduleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalRedeliverySchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRegionalRedeliveryScheduleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalRedeliveryScheduleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalRedeliverySchedule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionalRedeliveryScheduleRetriesOffsetAdjuster(
    var weight: Double,
) : RegionalRedeliveryScheduleModifier {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: RegionalRedeliverySchedule, value: Double): Double {
        return value + item.retries.toDouble() * weight
    }
}

data class FlaggedRegionalRedeliveryScheduleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalRedeliveryScheduleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalRedeliverySchedule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalRedeliveryScheduleAdjuster(var cap: Double) : RegionalRedeliveryScheduleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalRedeliverySchedule, value: Double): Double = minOf(value, cap)
}

data class MinimumRegionalRedeliveryScheduleAdjuster(var minimum: Double) : RegionalRedeliveryScheduleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalRedeliverySchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional redelivery schedule steps in order and keeps a trace of each. */
data class RegionalRedeliveryScheduleEngine(
    var steps: MutableList<RegionalRedeliveryScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalRedeliverySchedule): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalRedeliveryScheduleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalRedeliverySchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalRedeliveryScheduleEngine(flags: Registry): RegionalRedeliveryScheduleEngine {
    return RegionalRedeliveryScheduleEngine(
        steps = mutableListOf<RegionalRedeliveryScheduleModifier>(
            PercentRegionalRedeliveryScheduleAdjuster(18.3),
            TieredRegionalRedeliveryScheduleAdjuster(
                mutableListOf<Double>(2431.0, 3372.0, 9276.0, 9965.0),
                mutableListOf<Double>(0.94, 1.14, 0.95, 1.05),
            ),
            RegionalRedeliveryScheduleRetriesOffsetAdjuster(0.94),
            FlaggedRegionalRedeliveryScheduleAdjuster(flags, "regional-redelivery-schedule-tiered", 5.9),
            CapRegionalRedeliveryScheduleAdjuster(15549.0),
            MinimumRegionalRedeliveryScheduleAdjuster(11.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 3119.3,
    )
}
