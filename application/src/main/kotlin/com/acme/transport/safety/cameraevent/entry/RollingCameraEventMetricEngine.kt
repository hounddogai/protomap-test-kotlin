package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling camera event metric calculation. */
interface RollingCameraEventMetricAdjuster {
    fun describe(): String
    fun adjust(item: RollingCameraEventMetric, value: Double): Double
}

data class CapRollingCameraEventMetricAdjuster(var cap: Double) : RollingCameraEventMetricAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingCameraEventMetric, value: Double): Double = minOf(value, cap)
}

data class RoundingRollingCameraEventMetricAdjuster(var step: Double) : RollingCameraEventMetricAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingCameraEventMetric, value: Double): Double = roundTo(value, step)
}

data class PercentRollingCameraEventMetricAdjuster(var percent: Double) : RollingCameraEventMetricAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingCameraEventMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRollingCameraEventMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingCameraEventMetricAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingCameraEventMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LaneRollingCameraEventMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingCameraEventMetricAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: RollingCameraEventMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RollingCameraEventMetricAttemptsOffsetAdjuster(
    var weight: Double,
) : RollingCameraEventMetricAdjuster {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: RollingCameraEventMetric, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

/** Runs the rolling camera event metric steps in order and keeps a trace of each. */
data class RollingCameraEventMetricEngine(
    var steps: MutableList<RollingCameraEventMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingCameraEventMetric): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingCameraEventMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingCameraEventMetricEngine(): RollingCameraEventMetricEngine {
    return RollingCameraEventMetricEngine(
        steps = mutableListOf<RollingCameraEventMetricAdjuster>(
            CapRollingCameraEventMetricAdjuster(48562.0),
            RoundingRollingCameraEventMetricAdjuster(0.5),
            PercentRollingCameraEventMetricAdjuster(9.7),
            TieredRollingCameraEventMetricAdjuster(
                mutableListOf<Double>(935.0, 2635.0, 2894.0, 7932.0),
                mutableListOf<Double>(0.99, 1.17, 0.87, 1.14),
            ),
            LaneRollingCameraEventMetricAdjuster(mutableMapOf<String, Double>("online" to 0.89)),
            RollingCameraEventMetricAttemptsOffsetAdjuster(0.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 41794.8,
    )
}
