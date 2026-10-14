package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.flags.Registry

/** One step of the transit time metric calculation. */
interface TransitTimeMetricFactor {
    fun describe(): String
    fun adjust(item: TransitTimeMetric, value: Double): Double
}

data class TransitTimeMetricVersionOffsetAdjuster(var weight: Double) : TransitTimeMetricFactor {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: TransitTimeMetric, value: Double): Double = value + item.version.toDouble() * weight
}

data class TieredTransitTimeMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TransitTimeMetricFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TransitTimeMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTransitTimeMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TransitTimeMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TransitTimeMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTransitTimeMetricAdjuster(var minimum: Double) : TransitTimeMetricFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the transit time metric steps in order and keeps a trace of each. */
data class TransitTimeMetricEngine(
    var steps: MutableList<TransitTimeMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TransitTimeMetric): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TransitTimeMetricFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TransitTimeMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTransitTimeMetricEngine(flags: Registry): TransitTimeMetricEngine {
    return TransitTimeMetricEngine(
        steps = mutableListOf<TransitTimeMetricFactor>(
            TransitTimeMetricVersionOffsetAdjuster(0.35),
            TieredTransitTimeMetricAdjuster(
                mutableListOf<Double>(7224.0, 9292.0),
                mutableListOf<Double>(1.18, 1.04),
            ),
            FlaggedTransitTimeMetricAdjuster(flags, "transit-time-metric-weighted", -8.5),
            MinimumTransitTimeMetricAdjuster(25.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 77710.9,
    )
}
