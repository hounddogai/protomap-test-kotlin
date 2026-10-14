package com.acme.transport.lastmile.attempt.attemptmetric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional attempt metric calculation. */
interface RegionalAttemptMetricModifier {
    fun describe(): String
    fun adjust(item: RegionalAttemptMetric, value: Double): Double
}

data class RegionalAttemptMetricDiscountOffsetAdjuster(var weight: Double) : RegionalAttemptMetricModifier {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: RegionalAttemptMetric, value: Double): Double = value + item.discount * weight
}

data class FlaggedRegionalAttemptMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalAttemptMetricModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalAttemptMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRegionalAttemptMetricAdjuster(var step: Double) : RegionalAttemptMetricModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalAttemptMetric, value: Double): Double = roundTo(value, step)
}

/** Runs the regional attempt metric steps in order and keeps a trace of each. */
data class RegionalAttemptMetricEngine(
    var steps: MutableList<RegionalAttemptMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalAttemptMetric): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalAttemptMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalAttemptMetricEngine(flags: Registry): RegionalAttemptMetricEngine {
    return RegionalAttemptMetricEngine(
        steps = mutableListOf<RegionalAttemptMetricModifier>(
            RegionalAttemptMetricDiscountOffsetAdjuster(0.67),
            FlaggedRegionalAttemptMetricAdjuster(flags, "regional-attempt-metric-tiered", -6.4),
            RoundingRegionalAttemptMetricAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 14315.2,
    )
}
