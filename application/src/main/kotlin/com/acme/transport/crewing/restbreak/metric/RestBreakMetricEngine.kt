package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.kit.numbers.roundTo

/** One step of the rest break metric calculation. */
interface RestBreakMetricStep {
    fun describe(): String
    fun adjust(item: RestBreakMetric, value: Double): Double
}

data class RoundingRestBreakMetricAdjuster(var step: Double) : RestBreakMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakMetric, value: Double): Double = roundTo(value, step)
}

data class TieredRestBreakMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionRestBreakMetricAdjuster(var factors: MutableMap<String, Double>) : RestBreakMetricStep {
    override fun describe(): String = "by region"

    override fun adjust(item: RestBreakMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapRestBreakMetricAdjuster(var cap: Double) : RestBreakMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RestBreakMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the rest break metric steps in order and keeps a trace of each. */
data class RestBreakMetricEngine(
    var steps: MutableList<RestBreakMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RestBreakMetric): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RestBreakMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<RestBreakMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRestBreakMetricEngine(): RestBreakMetricEngine {
    return RestBreakMetricEngine(
        steps = mutableListOf<RestBreakMetricStep>(
            RoundingRestBreakMetricAdjuster(5.0),
            TieredRestBreakMetricAdjuster(
                mutableListOf<Double>(8595.0, 9395.0),
                mutableListOf<Double>(1.01, 1.2),
            ),
            RegionRestBreakMetricAdjuster(mutableMapOf<String, Double>("online" to 0.83)),
            CapRestBreakMetricAdjuster(4073.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 85786.9,
    )
}
