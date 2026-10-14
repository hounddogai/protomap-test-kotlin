package com.acme.transport.fleetops.tire.window

import com.acme.transport.kit.numbers.roundTo

/** One step of the tire request calculation. */
interface TireRequestModifier {
    fun describe(): String
    fun adjust(item: TireRequest, value: Double): Double
}

data class RoundingTireRequestAdjuster(var step: Double) : TireRequestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TireRequest, value: Double): Double = roundTo(value, step)
}

data class MinimumTireRequestAdjuster(var minimum: Double) : TireRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TireRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTireRequestAdjuster(var cap: Double) : TireRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TireRequest, value: Double): Double = minOf(value, cap)
}

/** Runs the tire request steps in order and keeps a trace of each. */
data class TireRequestEngine(
    var steps: MutableList<TireRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TireRequest): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TireRequestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TireRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTireRequestEngine(): TireRequestEngine {
    return TireRequestEngine(
        steps = mutableListOf<TireRequestModifier>(
            RoundingTireRequestAdjuster(0.05),
            MinimumTireRequestAdjuster(8.6),
            CapTireRequestAdjuster(31769.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 96412.8,
    )
}
