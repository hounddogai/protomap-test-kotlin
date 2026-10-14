package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled hold summary calculation. */
interface ScheduledHoldSummaryAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledHoldSummary, value: Double): Double
}

data class CapScheduledHoldSummaryAdjuster(var cap: Double) : ScheduledHoldSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledHoldSummary, value: Double): Double = minOf(value, cap)
}

data class MinimumScheduledHoldSummaryAdjuster(var minimum: Double) : ScheduledHoldSummaryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledHoldSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedScheduledHoldSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledHoldSummaryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledHoldSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled hold summary steps in order and keeps a trace of each. */
data class ScheduledHoldSummaryEngine(
    var steps: MutableList<ScheduledHoldSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledHoldSummary): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledHoldSummaryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledHoldSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledHoldSummaryEngine(flags: Registry): ScheduledHoldSummaryEngine {
    return ScheduledHoldSummaryEngine(
        steps = mutableListOf<ScheduledHoldSummaryAdjuster>(
            CapScheduledHoldSummaryAdjuster(18000.0),
            MinimumScheduledHoldSummaryAdjuster(22.4),
            FlaggedScheduledHoldSummaryAdjuster(flags, "scheduled-hold-summary-batched", -4.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 8585.2,
    )
}
