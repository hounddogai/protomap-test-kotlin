package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.numbers.roundTo

/** One step of the default locker metric calculation. */
interface DefaultLockerMetricModifier {
    fun describe(): String
    fun adjust(item: DefaultLockerMetric, value: Double): Double
}

data class LocaleDefaultLockerMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultLockerMetricModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: DefaultLockerMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentDefaultLockerMetricAdjuster(var percent: Double) : DefaultLockerMetricModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultLockerMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDefaultLockerMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultLockerMetricModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultLockerMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingDefaultLockerMetricAdjuster(var step: Double) : DefaultLockerMetricModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultLockerMetric, value: Double): Double = roundTo(value, step)
}

data class CapDefaultLockerMetricAdjuster(var cap: Double) : DefaultLockerMetricModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultLockerMetric, value: Double): Double = minOf(value, cap)
}

/** Runs the default locker metric steps in order and keeps a trace of each. */
data class DefaultLockerMetricEngine(
    var steps: MutableList<DefaultLockerMetricModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultLockerMetric): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultLockerMetricModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultLockerMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultLockerMetricEngine(): DefaultLockerMetricEngine {
    return DefaultLockerMetricEngine(
        steps = mutableListOf<DefaultLockerMetricModifier>(
            LocaleDefaultLockerMetricAdjuster(mutableMapOf<String, Double>("online" to 1.39)),
            PercentDefaultLockerMetricAdjuster(5.4),
            TieredDefaultLockerMetricAdjuster(
                mutableListOf<Double>(4430.0, 6989.0),
                mutableListOf<Double>(1.05, 0.95),
            ),
            RoundingDefaultLockerMetricAdjuster(1.0),
            CapDefaultLockerMetricAdjuster(13916.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 30787.8,
    )
}
