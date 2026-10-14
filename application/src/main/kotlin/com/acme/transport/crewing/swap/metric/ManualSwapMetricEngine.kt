package com.acme.transport.crewing.swap.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual swap metric calculation. */
interface ManualSwapMetricAdjuster {
    fun describe(): String
    fun adjust(item: ManualSwapMetric, value: Double): Double
}

data class FlaggedManualSwapMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualSwapMetricAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualSwapMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentManualSwapMetricAdjuster(var percent: Double) : ManualSwapMetricAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualSwapMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingManualSwapMetricAdjuster(var step: Double) : ManualSwapMetricAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualSwapMetric, value: Double): Double = roundTo(value, step)
}

data class DepotManualSwapMetricAdjuster(var factors: MutableMap<String, Double>) : ManualSwapMetricAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: ManualSwapMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumManualSwapMetricAdjuster(var minimum: Double) : ManualSwapMetricAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualSwapMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapManualSwapMetricAdjuster(var cap: Double) : ManualSwapMetricAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualSwapMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the manual swap metric steps in order and keeps a trace of each. */
data class ManualSwapMetricEngine(
    var steps: MutableList<ManualSwapMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualSwapMetric): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualSwapMetricAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualSwapMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualSwapMetricEngine(flags: Registry): ManualSwapMetricEngine {
    return ManualSwapMetricEngine(
        steps = mutableListOf<ManualSwapMetricAdjuster>(
            FlaggedManualSwapMetricAdjuster(flags, "manual-swap-metric-scheduled", -2.3),
            PercentManualSwapMetricAdjuster(-16.3),
            RoundingManualSwapMetricAdjuster(0.25),
            DepotManualSwapMetricAdjuster(
                mutableMapOf<String, Double>("retail" to 0.92, "online" to 1.39, "central" to 0.98),
            ),
            MinimumManualSwapMetricAdjuster(28.8),
            CapManualSwapMetricAdjuster(15161.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 79538.5,
    )
}
