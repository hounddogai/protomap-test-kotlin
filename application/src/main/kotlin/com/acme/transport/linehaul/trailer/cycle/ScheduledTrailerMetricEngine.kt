package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled trailer metric calculation. */
interface ScheduledTrailerMetricStep {
    fun describe(): String
    fun adjust(item: ScheduledTrailerMetric, value: Double): Double
}

data class SourceScheduledTrailerMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledTrailerMetricStep {
    override fun describe(): String = "by source"

    override fun adjust(item: ScheduledTrailerMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScheduledTrailerMetricAdjuster(var cap: Double) : ScheduledTrailerMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledTrailerMetric, value: Double): Double = minOf(value, cap)
}

data class FlaggedScheduledTrailerMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledTrailerMetricStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledTrailerMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the scheduled trailer metric steps in order and keeps a trace of each. */
data class ScheduledTrailerMetricEngine(
    var steps: MutableList<ScheduledTrailerMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledTrailerMetric): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledTrailerMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledTrailerMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledTrailerMetricEngine(flags: Registry): ScheduledTrailerMetricEngine {
    return ScheduledTrailerMetricEngine(
        steps = mutableListOf<ScheduledTrailerMetricStep>(
            SourceScheduledTrailerMetricAdjuster(mutableMapOf<String, Double>("east" to 0.75)),
            CapScheduledTrailerMetricAdjuster(8503.0),
            FlaggedScheduledTrailerMetricAdjuster(flags, "scheduled-trailer-metric-primary", 4.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 59741.6,
    )
}
