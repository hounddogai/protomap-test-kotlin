package com.acme.transport.lastmile.attempt.window

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default attempt metric calculation. */
interface DefaultAttemptMetricFactor {
    fun describe(): String
    fun adjust(item: DefaultAttemptMetric, value: Double): Double
}

data class RoundingDefaultAttemptMetricAdjuster(var step: Double) : DefaultAttemptMetricFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultAttemptMetric, value: Double): Double = roundTo(value, step)
}

data class PercentDefaultAttemptMetricAdjuster(var percent: Double) : DefaultAttemptMetricFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultAttemptMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDefaultAttemptMetricAdjuster(var cap: Double) : DefaultAttemptMetricFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultAttemptMetric, value: Double): Double = minOf(value, cap)
}

data class TieredDefaultAttemptMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultAttemptMetricFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultAttemptMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default attempt metric steps in order and keeps a trace of each. */
data class DefaultAttemptMetricEngine(
    var steps: MutableList<DefaultAttemptMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultAttemptMetric): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultAttemptMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultAttemptMetricEngine(): DefaultAttemptMetricEngine {
    return DefaultAttemptMetricEngine(
        steps = mutableListOf<DefaultAttemptMetricFactor>(
            RoundingDefaultAttemptMetricAdjuster(5.0),
            PercentDefaultAttemptMetricAdjuster(11.4),
            CapDefaultAttemptMetricAdjuster(44247.0),
            TieredDefaultAttemptMetricAdjuster(
                mutableListOf<Double>(2958.0, 7304.0, 7334.0, 9987.0),
                mutableListOf<Double>(0.94, 0.91, 0.91, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 89226.2,
    )
}
