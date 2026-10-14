package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending capacity metric calculation. */
interface PendingCapacityMetricFactor {
    fun describe(): String
    fun adjust(item: PendingCapacityMetric, value: Double): Double
}

data class CapPendingCapacityMetricAdjuster(var cap: Double) : PendingCapacityMetricFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingCapacityMetric, value: Double): Double = minOf(value, cap)
}

data class PercentPendingCapacityMetricAdjuster(var percent: Double) : PendingCapacityMetricFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingCapacityMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingCapacityMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingCapacityMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingCapacityMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPendingCapacityMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingCapacityMetricFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingCapacityMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PendingCapacityMetricScoreOffsetAdjuster(var weight: Double) : PendingCapacityMetricFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: PendingCapacityMetric, value: Double): Double = value + item.score * weight
}

data class RoundingPendingCapacityMetricAdjuster(var step: Double) : PendingCapacityMetricFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingCapacityMetric, value: Double): Double = roundTo(value, step)
}

/** Runs the pending capacity metric steps in order and keeps a trace of each. */
data class PendingCapacityMetricEngine(
    var steps: MutableList<PendingCapacityMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingCapacityMetric): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingCapacityMetricFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingCapacityMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingCapacityMetricEngine(flags: Registry): PendingCapacityMetricEngine {
    return PendingCapacityMetricEngine(
        steps = mutableListOf<PendingCapacityMetricFactor>(
            CapPendingCapacityMetricAdjuster(49705.0),
            PercentPendingCapacityMetricAdjuster(-1.4),
            FlaggedPendingCapacityMetricAdjuster(flags, "pending-capacity-metric-batched", -1.4),
            TieredPendingCapacityMetricAdjuster(
                mutableListOf<Double>(532.0, 6215.0, 8116.0, 9552.0),
                mutableListOf<Double>(1.04, 0.95, 1.0, 1.07),
            ),
            PendingCapacityMetricScoreOffsetAdjuster(0.72),
            RoundingPendingCapacityMetricAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 70769.6,
    )
}
