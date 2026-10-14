package com.acme.transport.safety.violation.quota

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the violation metric calculation. */
interface ViolationMetricStep {
    fun describe(): String
    fun adjust(item: ViolationMetric, value: Double): Double
}

data class UnitViolationMetricAdjuster(var factors: MutableMap<String, Double>) : ViolationMetricStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: ViolationMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredViolationMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationMetricStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapViolationMetricAdjuster(var cap: Double) : ViolationMetricStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationMetric, value: Double): Double = minOf(value, cap)
}

data class RoundingViolationMetricAdjuster(var step: Double) : ViolationMetricStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationMetric, value: Double): Double = roundTo(value, step)
}

/** Runs the violation metric steps in order and keeps a trace of each. */
data class ViolationMetricEngine(
    var steps: MutableList<ViolationMetricStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ViolationMetric): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultViolationMetricEngine(): ViolationMetricEngine {
    return ViolationMetricEngine(
        steps = mutableListOf<ViolationMetricStep>(
            UnitViolationMetricAdjuster(
                mutableMapOf<String, Double>("north" to 1.19, "retail" to 1.05, "east" to 1.33),
            ),
            TieredViolationMetricAdjuster(
                mutableListOf<Double>(6348.0, 6592.0),
                mutableListOf<Double>(0.85, 0.86),
            ),
            CapViolationMetricAdjuster(46038.0),
            RoundingViolationMetricAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 76835.7,
    )
}
