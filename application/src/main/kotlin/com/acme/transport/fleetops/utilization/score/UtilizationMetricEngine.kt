package com.acme.transport.fleetops.utilization.score

import com.acme.transport.kit.flags.Registry

/** One step of the utilization metric calculation. */
interface UtilizationMetricFactor {
    fun describe(): String
    fun adjust(item: UtilizationMetric, value: Double): Double
}

data class CapUtilizationMetricAdjuster(var cap: Double) : UtilizationMetricFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: UtilizationMetric, value: Double): Double = minOf(value, cap)
}

data class FlaggedUtilizationMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class UtilizationMetricDurationMinutesOffsetAdjuster(var weight: Double) : UtilizationMetricFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: UtilizationMetric, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class TieredUtilizationMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationMetricFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CountryUtilizationMetricAdjuster(var factors: MutableMap<String, Double>) : UtilizationMetricFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: UtilizationMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentUtilizationMetricAdjuster(var percent: Double) : UtilizationMetricFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the utilization metric steps in order and keeps a trace of each. */
data class UtilizationMetricEngine(
    var steps: MutableList<UtilizationMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: UtilizationMetric): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: UtilizationMetricFactor = steps[index]
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

    fun evaluateAll(items: MutableList<UtilizationMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultUtilizationMetricEngine(flags: Registry): UtilizationMetricEngine {
    return UtilizationMetricEngine(
        steps = mutableListOf<UtilizationMetricFactor>(
            CapUtilizationMetricAdjuster(6618.0),
            FlaggedUtilizationMetricAdjuster(flags, "utilization-metric-manual", -8.6),
            UtilizationMetricDurationMinutesOffsetAdjuster(0.43),
            TieredUtilizationMetricAdjuster(
                mutableListOf<Double>(3714.0, 3969.0, 4670.0),
                mutableListOf<Double>(0.96, 1.14, 0.91),
            ),
            CountryUtilizationMetricAdjuster(
                mutableMapOf<String, Double>("online" to 1.19, "central" to 1.01, "retail" to 1.06),
            ),
            PercentUtilizationMetricAdjuster(16.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 77236.3,
    )
}
