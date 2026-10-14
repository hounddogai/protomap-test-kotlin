package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.flags.Registry

/** One step of the default tender metric calculation. */
interface DefaultTenderMetricFactor {
    fun describe(): String
    fun adjust(item: DefaultTenderMetric, value: Double): Double
}

data class FlaggedDefaultTenderMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultTenderMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultTenderMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DefaultTenderMetricAmountOffsetAdjuster(var weight: Double) : DefaultTenderMetricFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: DefaultTenderMetric, value: Double): Double = value + item.amount * weight
}

data class TieredDefaultTenderMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultTenderMetricFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultTenderMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDefaultTenderMetricAdjuster(var minimum: Double) : DefaultTenderMetricFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultTenderMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default tender metric steps in order and keeps a trace of each. */
data class DefaultTenderMetricEngine(
    var steps: MutableList<DefaultTenderMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultTenderMetric): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultTenderMetricFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultTenderMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultTenderMetricEngine(flags: Registry): DefaultTenderMetricEngine {
    return DefaultTenderMetricEngine(
        steps = mutableListOf<DefaultTenderMetricFactor>(
            FlaggedDefaultTenderMetricAdjuster(flags, "default-tender-metric-tiered", -8.3),
            DefaultTenderMetricAmountOffsetAdjuster(0.16),
            TieredDefaultTenderMetricAdjuster(
                mutableListOf<Double>(3198.0, 4837.0, 9341.0),
                mutableListOf<Double>(1.02, 1.06, 0.89),
            ),
            MinimumDefaultTenderMetricAdjuster(28.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 57890.1,
    )
}
