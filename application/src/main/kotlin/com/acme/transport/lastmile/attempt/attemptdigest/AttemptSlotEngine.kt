package com.acme.transport.lastmile.attempt.attemptdigest

import com.acme.transport.kit.numbers.roundTo

/** One step of the attempt slot calculation. */
interface AttemptSlotStep {
    fun describe(): String
    fun adjust(item: AttemptSlot, value: Double): Double
}

data class RoundingAttemptSlotAdjuster(var step: Double) : AttemptSlotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AttemptSlot, value: Double): Double = roundTo(value, step)
}

data class TieredAttemptSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptSlotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAttemptSlotAdjuster(var cap: Double) : AttemptSlotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AttemptSlot, value: Double): Double = minOf(value, cap)
}

/** Runs the attempt slot steps in order and keeps a trace of each. */
data class AttemptSlotEngine(
    var steps: MutableList<AttemptSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AttemptSlot): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AttemptSlotStep = steps[index]
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

    fun evaluateAll(items: MutableList<AttemptSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAttemptSlotEngine(): AttemptSlotEngine {
    return AttemptSlotEngine(
        steps = mutableListOf<AttemptSlotStep>(
            RoundingAttemptSlotAdjuster(0.05),
            TieredAttemptSlotAdjuster(
                mutableListOf<Double>(1310.0, 1532.0, 2051.0, 6880.0),
                mutableListOf<Double>(1.03, 1.19, 0.82, 1.1),
            ),
            CapAttemptSlotAdjuster(23619.0),
        ),
        trace = mutableListOf<String>(),
        floor = 5.0,
        ceiling = 11069.1,
    )
}
