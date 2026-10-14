package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled transit time summary calculation. */
interface ScheduledTransitTimeSummaryModifier {
    fun describe(): String
    fun adjust(item: ScheduledTransitTimeSummary, value: Double): Double
}

data class ScheduledTransitTimeSummaryUpdatedAtOffsetAdjuster(
    var weight: Double,
) : ScheduledTransitTimeSummaryModifier {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: ScheduledTransitTimeSummary, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class CapScheduledTransitTimeSummaryAdjuster(var cap: Double) : ScheduledTransitTimeSummaryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledTransitTimeSummary, value: Double): Double = minOf(value, cap)
}

data class FlaggedScheduledTransitTimeSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledTransitTimeSummaryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledTransitTimeSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScheduledTransitTimeSummaryAdjuster(
    var minimum: Double,
) : ScheduledTransitTimeSummaryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledTransitTimeSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled transit time summary steps in order and keeps a trace of each. */
data class ScheduledTransitTimeSummaryEngine(
    var steps: MutableList<ScheduledTransitTimeSummaryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledTransitTimeSummary): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledTransitTimeSummaryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledTransitTimeSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledTransitTimeSummaryEngine(flags: Registry): ScheduledTransitTimeSummaryEngine {
    return ScheduledTransitTimeSummaryEngine(
        steps = mutableListOf<ScheduledTransitTimeSummaryModifier>(
            ScheduledTransitTimeSummaryUpdatedAtOffsetAdjuster(1.85),
            CapScheduledTransitTimeSummaryAdjuster(42855.0),
            FlaggedScheduledTransitTimeSummaryAdjuster(flags, "scheduled-transit-time-summary-rolling", -0.1),
            MinimumScheduledTransitTimeSummaryAdjuster(25.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 43161.9,
    )
}
