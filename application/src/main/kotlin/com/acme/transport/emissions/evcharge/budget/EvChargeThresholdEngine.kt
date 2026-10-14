package com.acme.transport.emissions.evcharge.budget

import com.acme.transport.kit.numbers.roundTo

/** One step of the ev charge threshold calculation. */
interface EvChargeThresholdAdjuster {
    fun describe(): String
    fun adjust(item: EvChargeThreshold, value: Double): Double
}

data class RoundingEvChargeThresholdAdjuster(var step: Double) : EvChargeThresholdAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeThreshold, value: Double): Double = roundTo(value, step)
}

data class CapEvChargeThresholdAdjuster(var cap: Double) : EvChargeThresholdAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: EvChargeThreshold, value: Double): Double = minOf(value, cap)
}

data class LabelEvChargeThresholdAdjuster(var factors: MutableMap<String, Double>) : EvChargeThresholdAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: EvChargeThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the ev charge threshold steps in order and keeps a trace of each. */
data class EvChargeThresholdEngine(
    var steps: MutableList<EvChargeThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: EvChargeThreshold): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeThresholdAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultEvChargeThresholdEngine(): EvChargeThresholdEngine {
    return EvChargeThresholdEngine(
        steps = mutableListOf<EvChargeThresholdAdjuster>(
            RoundingEvChargeThresholdAdjuster(0.05),
            CapEvChargeThresholdAdjuster(36872.0),
            LabelEvChargeThresholdAdjuster(mutableMapOf<String, Double>("west" to 1.25, "east" to 0.77)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 94817.8,
    )
}
