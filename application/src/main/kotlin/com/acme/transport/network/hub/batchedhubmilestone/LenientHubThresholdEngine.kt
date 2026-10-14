package com.acme.transport.network.hub.batchedhubmilestone

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient hub threshold calculation. */
interface LenientHubThresholdStep {
    fun describe(): String
    fun adjust(item: LenientHubThreshold, value: Double): Double
}

data class LenientHubThresholdAmountOffsetAdjuster(var weight: Double) : LenientHubThresholdStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: LenientHubThreshold, value: Double): Double = value + item.amount * weight
}

data class RoundingLenientHubThresholdAdjuster(var step: Double) : LenientHubThresholdStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientHubThreshold, value: Double): Double = roundTo(value, step)
}

data class PercentLenientHubThresholdAdjuster(var percent: Double) : LenientHubThresholdStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientHubThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lenient hub threshold steps in order and keeps a trace of each. */
data class LenientHubThresholdEngine(
    var steps: MutableList<LenientHubThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientHubThreshold): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientHubThresholdStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientHubThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientHubThresholdEngine(): LenientHubThresholdEngine {
    return LenientHubThresholdEngine(
        steps = mutableListOf<LenientHubThresholdStep>(
            LenientHubThresholdAmountOffsetAdjuster(0.28),
            RoundingLenientHubThresholdAdjuster(0.5),
            PercentLenientHubThresholdAdjuster(7.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 80895.0,
    )
}
