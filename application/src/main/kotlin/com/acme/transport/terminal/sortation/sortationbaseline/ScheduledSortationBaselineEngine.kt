package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled sortation baseline calculation. */
interface ScheduledSortationBaselineModifier {
    fun describe(): String
    fun adjust(item: ScheduledSortationBaseline, value: Double): Double
}

data class FlaggedScheduledSortationBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledSortationBaselineModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledSortationBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScheduledSortationBaselineAdjuster(var percent: Double) : ScheduledSortationBaselineModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledSortationBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredScheduledSortationBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledSortationBaselineModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledSortationBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled sortation baseline steps in order and keeps a trace of each. */
data class ScheduledSortationBaselineEngine(
    var steps: MutableList<ScheduledSortationBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledSortationBaseline): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledSortationBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledSortationBaselineEngine(flags: Registry): ScheduledSortationBaselineEngine {
    return ScheduledSortationBaselineEngine(
        steps = mutableListOf<ScheduledSortationBaselineModifier>(
            FlaggedScheduledSortationBaselineAdjuster(flags, "scheduled-sortation-baseline-rolling", 5.7),
            PercentScheduledSortationBaselineAdjuster(2.3),
            TieredScheduledSortationBaselineAdjuster(
                mutableListOf<Double>(2990.0, 4270.0, 7667.0, 8261.0),
                mutableListOf<Double>(1.01, 1.1, 1.06, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 50421.9,
    )
}
