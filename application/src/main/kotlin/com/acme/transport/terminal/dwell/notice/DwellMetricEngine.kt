package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the dwell metric calculation. */
interface DwellMetricStep {
    fun describe(): String
    fun adjust(item: DwellMetric, value: Double): Double
}

data class PercentDwellMetricAdjuster(var percent: Double) : DwellMetricStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDwellMetricAdjuster(var minimum: Double) : DwellMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DwellMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDwellMetricAdjuster(var step: Double) : DwellMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellMetric, value: Double): Double = roundTo(value, step)
}

data class FlaggedDwellMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellMetricStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the dwell metric steps in order and keeps a trace of each. */
data class DwellMetricEngine(
    var steps: MutableList<DwellMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DwellMetric): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DwellMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<DwellMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDwellMetricEngine(flags: Registry): DwellMetricEngine {
    return DwellMetricEngine(
        steps = mutableListOf<DwellMetricStep>(
            PercentDwellMetricAdjuster(-17.0),
            MinimumDwellMetricAdjuster(29.4),
            RoundingDwellMetricAdjuster(0.05),
            FlaggedDwellMetricAdjuster(flags, "dwell-metric-manual", 3.5),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 51647.1,
    )
}
