package com.acme.transport.safety.violation.metric

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual violation metric calculation. */
interface ManualViolationMetricStep {
    fun describe(): String
    fun adjust(item: ManualViolationMetric, value: Double): Double
}

data class TieredManualViolationMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualViolationMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualViolationMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingManualViolationMetricAdjuster(var step: Double) : ManualViolationMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualViolationMetric, value: Double): Double = roundTo(value, step)
}

data class MinimumManualViolationMetricAdjuster(var minimum: Double) : ManualViolationMetricStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualViolationMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapManualViolationMetricAdjuster(var cap: Double) : ManualViolationMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualViolationMetric, value: Double): Double = minOf(value, cap)
}

data class UnitManualViolationMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualViolationMetricStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: ManualViolationMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentManualViolationMetricAdjuster(var percent: Double) : ManualViolationMetricStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualViolationMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the manual violation metric steps in order and keeps a trace of each. */
data class ManualViolationMetricEngine(
    var steps: MutableList<ManualViolationMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualViolationMetric): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualViolationMetricStep = steps[index]
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

    fun evaluateAll(items: MutableList<ManualViolationMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualViolationMetricEngine(): ManualViolationMetricEngine {
    return ManualViolationMetricEngine(
        steps = mutableListOf<ManualViolationMetricStep>(
            TieredManualViolationMetricAdjuster(
                mutableListOf<Double>(5052.0, 8142.0),
                mutableListOf<Double>(1.03, 0.94),
            ),
            RoundingManualViolationMetricAdjuster(0.5),
            MinimumManualViolationMetricAdjuster(21.2),
            CapManualViolationMetricAdjuster(3098.0),
            UnitManualViolationMetricAdjuster(mutableMapOf<String, Double>("north" to 0.91)),
            PercentManualViolationMetricAdjuster(1.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 8437.3,
    )
}
