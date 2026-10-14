package com.acme.transport.lastmile.redelivery.metric

import com.acme.transport.kit.flags.Registry

/** One step of the redelivery metric calculation. */
interface RedeliveryMetricModifier {
    fun describe(): String
    fun adjust(item: RedeliveryMetric, value: Double): Double
}

data class FlaggedRedeliveryMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RedeliveryMetricModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RedeliveryMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRedeliveryMetricAdjuster(var cap: Double) : RedeliveryMetricModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryMetric, value: Double): Double = minOf(value, cap)
}

data class TieredRedeliveryMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RedeliveryMetricModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RedeliveryMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RedeliveryMetricRetriesOffsetAdjuster(var weight: Double) : RedeliveryMetricModifier {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: RedeliveryMetric, value: Double): Double = value + item.retries.toDouble() * weight
}

data class PercentRedeliveryMetricAdjuster(var percent: Double) : RedeliveryMetricModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the redelivery metric steps in order and keeps a trace of each. */
data class RedeliveryMetricEngine(
    var steps: MutableList<RedeliveryMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RedeliveryMetric): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryMetricModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRedeliveryMetricEngine(flags: Registry): RedeliveryMetricEngine {
    return RedeliveryMetricEngine(
        steps = mutableListOf<RedeliveryMetricModifier>(
            FlaggedRedeliveryMetricAdjuster(flags, "redelivery-metric-capped", 0.9),
            CapRedeliveryMetricAdjuster(6237.0),
            TieredRedeliveryMetricAdjuster(
                mutableListOf<Double>(7502.0, 9339.0),
                mutableListOf<Double>(0.97, 0.99),
            ),
            RedeliveryMetricRetriesOffsetAdjuster(0.66),
            PercentRedeliveryMetricAdjuster(18.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 98335.8,
    )
}
