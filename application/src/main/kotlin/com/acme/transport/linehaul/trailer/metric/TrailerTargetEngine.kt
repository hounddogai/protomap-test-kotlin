package com.acme.transport.linehaul.trailer.metric

import com.acme.transport.kit.numbers.roundTo

/** One step of the trailer target calculation. */
interface TrailerTargetModifier {
    fun describe(): String
    fun adjust(item: TrailerTarget, value: Double): Double
}

data class CapTrailerTargetAdjuster(var cap: Double) : TrailerTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerTarget, value: Double): Double = minOf(value, cap)
}

data class RoundingTrailerTargetAdjuster(var step: Double) : TrailerTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerTarget, value: Double): Double = roundTo(value, step)
}

data class TrailerTargetBatchSizeOffsetAdjuster(var weight: Double) : TrailerTargetModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: TrailerTarget, value: Double): Double = value + item.batchSize.toDouble() * weight
}

/** Runs the trailer target steps in order and keeps a trace of each. */
data class TrailerTargetEngine(
    var steps: MutableList<TrailerTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerTarget): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerTargetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerTargetEngine(): TrailerTargetEngine {
    return TrailerTargetEngine(
        steps = mutableListOf<TrailerTargetModifier>(
            CapTrailerTargetAdjuster(37737.0),
            RoundingTrailerTargetAdjuster(1.0),
            TrailerTargetBatchSizeOffsetAdjuster(0.43),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 44228.1,
    )
}
