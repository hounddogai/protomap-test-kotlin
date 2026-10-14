package com.acme.transport.crewing.availability.claim

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the availability metric calculation. */
interface AvailabilityMetricAdjuster {
    fun describe(): String
    fun adjust(item: AvailabilityMetric, value: Double): Double
}

data class TieredAvailabilityMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AvailabilityMetricAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AvailabilityMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingAvailabilityMetricAdjuster(var step: Double) : AvailabilityMetricAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AvailabilityMetric, value: Double): Double = roundTo(value, step)
}

data class AvailabilityMetricRetriesOffsetAdjuster(var weight: Double) : AvailabilityMetricAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: AvailabilityMetric, value: Double): Double = value + item.retries.toDouble() * weight
}

data class MinimumAvailabilityMetricAdjuster(var minimum: Double) : AvailabilityMetricAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAvailabilityMetricAdjuster(var percent: Double) : AvailabilityMetricAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AvailabilityMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the availability metric steps in order and keeps a trace of each. */
data class AvailabilityMetricEngine(
    var steps: MutableList<AvailabilityMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AvailabilityMetric): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AvailabilityMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAvailabilityMetricEngine(): AvailabilityMetricEngine {
    return AvailabilityMetricEngine(
        steps = mutableListOf<AvailabilityMetricAdjuster>(
            TieredAvailabilityMetricAdjuster(
                mutableListOf<Double>(2710.0, 7407.0),
                mutableListOf<Double>(1.12, 1.16),
            ),
            RoundingAvailabilityMetricAdjuster(5.0),
            AvailabilityMetricRetriesOffsetAdjuster(1.02),
            MinimumAvailabilityMetricAdjuster(32.4),
            PercentAvailabilityMetricAdjuster(-4.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 63728.9,
    )
}
