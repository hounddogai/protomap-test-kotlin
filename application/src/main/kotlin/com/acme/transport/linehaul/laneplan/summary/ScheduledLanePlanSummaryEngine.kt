package com.acme.transport.linehaul.laneplan.summary

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled lane plan summary calculation. */
interface ScheduledLanePlanSummaryStep {
    fun describe(): String
    fun adjust(item: ScheduledLanePlanSummary, value: Double): Double
}

data class FlaggedScheduledLanePlanSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledLanePlanSummaryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledLanePlanSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScheduledLanePlanSummaryAdjuster(var minimum: Double) : ScheduledLanePlanSummaryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledLanePlanSummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredScheduledLanePlanSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledLanePlanSummaryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledLanePlanSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapScheduledLanePlanSummaryAdjuster(var cap: Double) : ScheduledLanePlanSummaryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledLanePlanSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled lane plan summary steps in order and keeps a trace of each. */
data class ScheduledLanePlanSummaryEngine(
    var steps: MutableList<ScheduledLanePlanSummaryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledLanePlanSummary): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledLanePlanSummaryStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledLanePlanSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledLanePlanSummaryEngine(flags: Registry): ScheduledLanePlanSummaryEngine {
    return ScheduledLanePlanSummaryEngine(
        steps = mutableListOf<ScheduledLanePlanSummaryStep>(
            FlaggedScheduledLanePlanSummaryAdjuster(flags, "scheduled-lane-plan-summary-lenient", -4.8),
            MinimumScheduledLanePlanSummaryAdjuster(13.5),
            TieredScheduledLanePlanSummaryAdjuster(
                mutableListOf<Double>(993.0, 5234.0, 8700.0),
                mutableListOf<Double>(0.96, 1.11, 1.19),
            ),
            CapScheduledLanePlanSummaryAdjuster(49718.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 24419.8,
    )
}
