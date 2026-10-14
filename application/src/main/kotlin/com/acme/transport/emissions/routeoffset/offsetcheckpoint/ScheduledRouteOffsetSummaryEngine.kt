package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled route offset summary calculation. */
interface ScheduledRouteOffsetSummaryFactor {
    fun describe(): String
    fun adjust(item: ScheduledRouteOffsetSummary, value: Double): Double
}

data class FlaggedScheduledRouteOffsetSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledRouteOffsetSummaryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledRouteOffsetSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScheduledRouteOffsetSummaryAdjuster(var percent: Double) : ScheduledRouteOffsetSummaryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledRouteOffsetSummary, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapScheduledRouteOffsetSummaryAdjuster(var cap: Double) : ScheduledRouteOffsetSummaryFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledRouteOffsetSummary, value: Double): Double = minOf(value, cap)
}

data class TieredScheduledRouteOffsetSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledRouteOffsetSummaryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledRouteOffsetSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumScheduledRouteOffsetSummaryAdjuster(var minimum: Double) : ScheduledRouteOffsetSummaryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledRouteOffsetSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingScheduledRouteOffsetSummaryAdjuster(var step: Double) : ScheduledRouteOffsetSummaryFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledRouteOffsetSummary, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled route offset summary steps in order and keeps a trace of each. */
data class ScheduledRouteOffsetSummaryEngine(
    var steps: MutableList<ScheduledRouteOffsetSummaryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledRouteOffsetSummary): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledRouteOffsetSummaryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledRouteOffsetSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledRouteOffsetSummaryEngine(flags: Registry): ScheduledRouteOffsetSummaryEngine {
    return ScheduledRouteOffsetSummaryEngine(
        steps = mutableListOf<ScheduledRouteOffsetSummaryFactor>(
            FlaggedScheduledRouteOffsetSummaryAdjuster(flags, "scheduled-route-offset-summary-lenient", -6.2),
            PercentScheduledRouteOffsetSummaryAdjuster(20.4),
            CapScheduledRouteOffsetSummaryAdjuster(49077.0),
            TieredScheduledRouteOffsetSummaryAdjuster(
                mutableListOf<Double>(23.0, 3715.0),
                mutableListOf<Double>(1.0, 1.19),
            ),
            MinimumScheduledRouteOffsetSummaryAdjuster(5.8),
            RoundingScheduledRouteOffsetSummaryAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 43501.3,
    )
}
