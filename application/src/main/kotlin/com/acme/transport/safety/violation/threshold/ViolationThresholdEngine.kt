package com.acme.transport.safety.violation.threshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the violation threshold calculation. */
interface ViolationThresholdModifier {
    fun describe(): String
    fun adjust(item: ViolationThreshold, value: Double): Double
}

data class RoundingViolationThresholdAdjuster(var step: Double) : ViolationThresholdModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationThreshold, value: Double): Double = roundTo(value, step)
}

data class CapViolationThresholdAdjuster(var cap: Double) : ViolationThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationThreshold, value: Double): Double = minOf(value, cap)
}

data class ChannelViolationThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : ViolationThresholdModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: ViolationThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the violation threshold steps in order and keeps a trace of each. */
data class ViolationThresholdEngine(
    var steps: MutableList<ViolationThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ViolationThreshold): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ViolationThresholdModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ViolationThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultViolationThresholdEngine(): ViolationThresholdEngine {
    return ViolationThresholdEngine(
        steps = mutableListOf<ViolationThresholdModifier>(
            RoundingViolationThresholdAdjuster(1.0),
            CapViolationThresholdAdjuster(34497.0),
            ChannelViolationThresholdAdjuster(mutableMapOf<String, Double>("retail" to 0.74, "west" to 1.36)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 70313.6,
    )
}
