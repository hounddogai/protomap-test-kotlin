package com.acme.transport.safety.violation.variance

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the violation variance calculation. */
interface ViolationVarianceFactor {
    fun describe(): String
    fun adjust(item: ViolationVariance, value: Double): Double
}

data class PercentViolationVarianceAdjuster(var percent: Double) : ViolationVarianceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredViolationVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationVarianceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingViolationVarianceAdjuster(var step: Double) : ViolationVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationVariance, value: Double): Double = roundTo(value, step)
}

/** Runs the violation variance steps in order and keeps a trace of each. */
data class ViolationVarianceEngine(
    var steps: MutableList<ViolationVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ViolationVariance): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultViolationVarianceEngine(): ViolationVarianceEngine {
    return ViolationVarianceEngine(
        steps = mutableListOf<ViolationVarianceFactor>(
            PercentViolationVarianceAdjuster(19.0),
            TieredViolationVarianceAdjuster(
                mutableListOf<Double>(979.0, 4975.0, 5479.0, 6330.0),
                mutableListOf<Double>(0.91, 0.83, 0.82, 1.11),
            ),
            RoundingViolationVarianceAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 68355.9,
    )
}
