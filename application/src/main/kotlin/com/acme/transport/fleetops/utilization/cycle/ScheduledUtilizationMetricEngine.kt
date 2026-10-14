package com.acme.transport.fleetops.utilization.cycle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled utilization metric calculation. */
interface ScheduledUtilizationMetricModifier {
    fun describe(): String
    fun adjust(item: ScheduledUtilizationMetric, value: Double): Double
}

data class ScheduledUtilizationMetricQuantityOffsetAdjuster(
    var weight: Double,
) : ScheduledUtilizationMetricModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: ScheduledUtilizationMetric, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class RoundingScheduledUtilizationMetricAdjuster(var step: Double) : ScheduledUtilizationMetricModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledUtilizationMetric, value: Double): Double = roundTo(value, step)
}

data class TieredScheduledUtilizationMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledUtilizationMetricModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledUtilizationMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentScheduledUtilizationMetricAdjuster(var percent: Double) : ScheduledUtilizationMetricModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledUtilizationMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapScheduledUtilizationMetricAdjuster(var cap: Double) : ScheduledUtilizationMetricModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledUtilizationMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled utilization metric steps in order and keeps a trace of each. */
data class ScheduledUtilizationMetricEngine(
    var steps: MutableList<ScheduledUtilizationMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledUtilizationMetric): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledUtilizationMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledUtilizationMetricEngine(): ScheduledUtilizationMetricEngine {
    return ScheduledUtilizationMetricEngine(
        steps = mutableListOf<ScheduledUtilizationMetricModifier>(
            ScheduledUtilizationMetricQuantityOffsetAdjuster(0.83),
            RoundingScheduledUtilizationMetricAdjuster(0.1),
            TieredScheduledUtilizationMetricAdjuster(
                mutableListOf<Double>(2305.0, 7231.0, 9590.0),
                mutableListOf<Double>(1.01, 0.86, 0.88),
            ),
            PercentScheduledUtilizationMetricAdjuster(19.6),
            CapScheduledUtilizationMetricAdjuster(27427.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 32385.5,
    )
}
