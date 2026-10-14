package com.acme.transport.safety.incident.defaultincidentmanifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict incident metric calculation. */
interface StrictIncidentMetricAdjuster {
    fun describe(): String
    fun adjust(item: StrictIncidentMetric, value: Double): Double
}

data class FlaggedStrictIncidentMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictIncidentMetricAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictIncidentMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredStrictIncidentMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictIncidentMetricAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictIncidentMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingStrictIncidentMetricAdjuster(var step: Double) : StrictIncidentMetricAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictIncidentMetric, value: Double): Double = roundTo(value, step)
}

data class MinimumStrictIncidentMetricAdjuster(var minimum: Double) : StrictIncidentMetricAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictIncidentMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelStrictIncidentMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictIncidentMetricAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: StrictIncidentMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapStrictIncidentMetricAdjuster(var cap: Double) : StrictIncidentMetricAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictIncidentMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the strict incident metric steps in order and keeps a trace of each. */
data class StrictIncidentMetricEngine(
    var steps: MutableList<StrictIncidentMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictIncidentMetric): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictIncidentMetricAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictIncidentMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictIncidentMetricEngine(flags: Registry): StrictIncidentMetricEngine {
    return StrictIncidentMetricEngine(
        steps = mutableListOf<StrictIncidentMetricAdjuster>(
            FlaggedStrictIncidentMetricAdjuster(flags, "strict-incident-metric-rolling", -6.6),
            TieredStrictIncidentMetricAdjuster(
                mutableListOf<Double>(5853.0, 6924.0, 7866.0),
                mutableListOf<Double>(0.81, 0.84, 1.03),
            ),
            RoundingStrictIncidentMetricAdjuster(5.0),
            MinimumStrictIncidentMetricAdjuster(16.3),
            LabelStrictIncidentMetricAdjuster(mutableMapOf<String, Double>("north" to 0.99)),
            CapStrictIncidentMetricAdjuster(5920.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 42191.2,
    )
}
