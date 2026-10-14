package com.acme.transport.customs.declaration.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the declaration metric calculation. */
interface DeclarationMetricFactor {
    fun describe(): String
    fun adjust(item: DeclarationMetric, value: Double): Double
}

data class CapDeclarationMetricAdjuster(var cap: Double) : DeclarationMetricFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DeclarationMetric, value: Double): Double = minOf(value, cap)
}

data class TitleDeclarationMetricAdjuster(var factors: MutableMap<String, Double>) : DeclarationMetricFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: DeclarationMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingDeclarationMetricAdjuster(var step: Double) : DeclarationMetricFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DeclarationMetric, value: Double): Double = roundTo(value, step)
}

data class TieredDeclarationMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DeclarationMetricFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DeclarationMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedDeclarationMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DeclarationMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DeclarationMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDeclarationMetricAdjuster(var percent: Double) : DeclarationMetricFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DeclarationMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the declaration metric steps in order and keeps a trace of each. */
data class DeclarationMetricEngine(
    var steps: MutableList<DeclarationMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DeclarationMetric): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DeclarationMetricFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DeclarationMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDeclarationMetricEngine(flags: Registry): DeclarationMetricEngine {
    return DeclarationMetricEngine(
        steps = mutableListOf<DeclarationMetricFactor>(
            CapDeclarationMetricAdjuster(46067.0),
            TitleDeclarationMetricAdjuster(
                mutableMapOf<String, Double>("retail" to 1.33, "north" to 1.3, "online" to 1.1),
            ),
            RoundingDeclarationMetricAdjuster(1.0),
            TieredDeclarationMetricAdjuster(
                mutableListOf<Double>(897.0, 2066.0, 7374.0, 8497.0),
                mutableListOf<Double>(0.92, 0.85, 1.04, 0.92),
            ),
            FlaggedDeclarationMetricAdjuster(flags, "declaration-metric-rolling", -5.1),
            PercentDeclarationMetricAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 27520.9,
    )
}
