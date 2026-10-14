package com.acme.transport.crewing.roster.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the roster metric calculation. */
interface RosterMetricAdjuster {
    fun describe(): String
    fun adjust(item: RosterMetric, value: Double): Double
}

data class FlaggedRosterMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RosterMetricAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RosterMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRosterMetricAdjuster(var step: Double) : RosterMetricAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RosterMetric, value: Double): Double = roundTo(value, step)
}

data class MinimumRosterMetricAdjuster(var minimum: Double) : RosterMetricAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RosterMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRosterMetricAdjuster(var percent: Double) : RosterMetricAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the roster metric steps in order and keeps a trace of each. */
data class RosterMetricEngine(
    var steps: MutableList<RosterMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RosterMetric): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterMetricAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RosterMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRosterMetricEngine(flags: Registry): RosterMetricEngine {
    return RosterMetricEngine(
        steps = mutableListOf<RosterMetricAdjuster>(
            FlaggedRosterMetricAdjuster(flags, "roster-metric-strict", 7.9),
            RoundingRosterMetricAdjuster(0.25),
            MinimumRosterMetricAdjuster(28.5),
            PercentRosterMetricAdjuster(-1.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 93081.1,
    )
}
