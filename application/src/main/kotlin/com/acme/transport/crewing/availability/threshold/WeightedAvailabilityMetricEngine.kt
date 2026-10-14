package com.acme.transport.crewing.availability.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted availability metric calculation. */
interface WeightedAvailabilityMetricFactor {
    fun describe(): String
    fun adjust(item: WeightedAvailabilityMetric, value: Double): Double
}

data class WeightedAvailabilityMetricDurationMinutesOffsetAdjuster(
    var weight: Double,
) : WeightedAvailabilityMetricFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: WeightedAvailabilityMetric, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class RoundingWeightedAvailabilityMetricAdjuster(var step: Double) : WeightedAvailabilityMetricFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedAvailabilityMetric, value: Double): Double = roundTo(value, step)
}

data class FlaggedWeightedAvailabilityMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedAvailabilityMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedAvailabilityMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentWeightedAvailabilityMetricAdjuster(var percent: Double) : WeightedAvailabilityMetricFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedAvailabilityMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumWeightedAvailabilityMetricAdjuster(var minimum: Double) : WeightedAvailabilityMetricFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedAvailabilityMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapWeightedAvailabilityMetricAdjuster(var cap: Double) : WeightedAvailabilityMetricFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedAvailabilityMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the weighted availability metric steps in order and keeps a trace of each. */
data class WeightedAvailabilityMetricEngine(
    var steps: MutableList<WeightedAvailabilityMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedAvailabilityMetric): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedAvailabilityMetricFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedAvailabilityMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedAvailabilityMetricEngine(flags: Registry): WeightedAvailabilityMetricEngine {
    return WeightedAvailabilityMetricEngine(
        steps = mutableListOf<WeightedAvailabilityMetricFactor>(
            WeightedAvailabilityMetricDurationMinutesOffsetAdjuster(1.5),
            RoundingWeightedAvailabilityMetricAdjuster(0.05),
            FlaggedWeightedAvailabilityMetricAdjuster(flags, "weighted-availability-metric-pending", -3.7),
            PercentWeightedAvailabilityMetricAdjuster(-24.4),
            MinimumWeightedAvailabilityMetricAdjuster(28.2),
            CapWeightedAvailabilityMetricAdjuster(3428.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 96242.1,
    )
}
