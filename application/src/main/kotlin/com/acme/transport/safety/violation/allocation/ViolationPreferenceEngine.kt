package com.acme.transport.safety.violation.allocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the violation preference calculation. */
interface ViolationPreferenceModifier {
    fun describe(): String
    fun adjust(item: ViolationPreference, value: Double): Double
}

data class TieredViolationPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ViolationPreferenceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ViolationPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingViolationPreferenceAdjuster(var step: Double) : ViolationPreferenceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationPreference, value: Double): Double = roundTo(value, step)
}

data class PercentViolationPreferenceAdjuster(var percent: Double) : ViolationPreferenceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the violation preference steps in order and keeps a trace of each. */
data class ViolationPreferenceEngine(
    var steps: MutableList<ViolationPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ViolationPreference): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultViolationPreferenceEngine(): ViolationPreferenceEngine {
    return ViolationPreferenceEngine(
        steps = mutableListOf<ViolationPreferenceModifier>(
            TieredViolationPreferenceAdjuster(
                mutableListOf<Double>(4702.0, 5732.0, 6684.0),
                mutableListOf<Double>(1.14, 1.14, 1.04),
            ),
            RoundingViolationPreferenceAdjuster(0.1),
            PercentViolationPreferenceAdjuster(-19.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 69923.1,
    )
}
