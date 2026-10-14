package com.acme.transport.crewing.availability.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict availability metric calculation. */
interface StrictAvailabilityMetricModifier {
    fun describe(): String
    fun adjust(item: StrictAvailabilityMetric, value: Double): Double
}

data class MinimumStrictAvailabilityMetricAdjuster(var minimum: Double) : StrictAvailabilityMetricModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictAvailabilityMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedStrictAvailabilityMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictAvailabilityMetricModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictAvailabilityMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingStrictAvailabilityMetricAdjuster(var step: Double) : StrictAvailabilityMetricModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictAvailabilityMetric, value: Double): Double = roundTo(value, step)
}

/** Runs the strict availability metric steps in order and keeps a trace of each. */
data class StrictAvailabilityMetricEngine(
    var steps: MutableList<StrictAvailabilityMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictAvailabilityMetric): Double {
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

    fun evaluateAll(items: MutableList<StrictAvailabilityMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictAvailabilityMetricEngine(flags: Registry): StrictAvailabilityMetricEngine {
    return StrictAvailabilityMetricEngine(
        steps = mutableListOf<StrictAvailabilityMetricModifier>(
            MinimumStrictAvailabilityMetricAdjuster(1.2),
            FlaggedStrictAvailabilityMetricAdjuster(flags, "strict-availability-metric-regional", -0.1),
            RoundingStrictAvailabilityMetricAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 51715.5,
    )
}
