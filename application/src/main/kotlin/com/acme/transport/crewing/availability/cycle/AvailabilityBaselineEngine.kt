package com.acme.transport.crewing.availability.cycle

import com.acme.transport.kit.flags.Registry

/** One step of the availability baseline calculation. */
interface AvailabilityBaselineModifier {
    fun describe(): String
    fun adjust(item: AvailabilityBaseline, value: Double): Double
}

data class MinimumAvailabilityBaselineAdjuster(var minimum: Double) : AvailabilityBaselineModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAvailabilityBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilityBaselineModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilityBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredAvailabilityBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AvailabilityBaselineModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AvailabilityBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAvailabilityBaselineAdjuster(var percent: Double) : AvailabilityBaselineModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AvailabilityBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapAvailabilityBaselineAdjuster(var cap: Double) : AvailabilityBaselineModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AvailabilityBaseline, value: Double): Double = minOf(value, cap)
}

/** Runs the availability baseline steps in order and keeps a trace of each. */
data class AvailabilityBaselineEngine(
    var steps: MutableList<AvailabilityBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AvailabilityBaseline): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AvailabilityBaselineModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AvailabilityBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAvailabilityBaselineEngine(flags: Registry): AvailabilityBaselineEngine {
    return AvailabilityBaselineEngine(
        steps = mutableListOf<AvailabilityBaselineModifier>(
            MinimumAvailabilityBaselineAdjuster(22.0),
            FlaggedAvailabilityBaselineAdjuster(flags, "availability-baseline-legacy", 6.1),
            TieredAvailabilityBaselineAdjuster(
                mutableListOf<Double>(3331.0, 5645.0, 8013.0, 9334.0),
                mutableListOf<Double>(1.1, 0.97, 0.81, 0.86),
            ),
            PercentAvailabilityBaselineAdjuster(-15.8),
            CapAvailabilityBaselineAdjuster(17206.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 53011.5,
    )
}
