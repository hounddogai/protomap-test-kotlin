package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient service area threshold calculation. */
interface LenientServiceAreaThresholdFactor {
    fun describe(): String
    fun adjust(item: LenientServiceAreaThreshold, value: Double): Double
}

data class PercentLenientServiceAreaThresholdAdjuster(var percent: Double) : LenientServiceAreaThresholdFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientServiceAreaThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLenientServiceAreaThresholdAdjuster(var minimum: Double) : LenientServiceAreaThresholdFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientServiceAreaThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLenientServiceAreaThresholdAdjuster(var step: Double) : LenientServiceAreaThresholdFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientServiceAreaThreshold, value: Double): Double = roundTo(value, step)
}

/** Runs the lenient service area threshold steps in order and keeps a trace of each. */
data class LenientServiceAreaThresholdEngine(
    var steps: MutableList<LenientServiceAreaThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientServiceAreaThreshold): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientServiceAreaThresholdFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientServiceAreaThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientServiceAreaThresholdEngine(): LenientServiceAreaThresholdEngine {
    return LenientServiceAreaThresholdEngine(
        steps = mutableListOf<LenientServiceAreaThresholdFactor>(
            PercentLenientServiceAreaThresholdAdjuster(-13.0),
            MinimumLenientServiceAreaThresholdAdjuster(38.1),
            RoundingLenientServiceAreaThresholdAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 45125.5,
    )
}
