package com.acme.transport.network.transittime.request

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the transit time request calculation. */
interface TransitTimeRequestStep {
    fun describe(): String
    fun adjust(item: TransitTimeRequest, value: Double): Double
}

data class MinimumTransitTimeRequestAdjuster(var minimum: Double) : TransitTimeRequestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TransitTimeRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingTransitTimeRequestAdjuster(var step: Double) : TransitTimeRequestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TransitTimeRequest, value: Double): Double = roundTo(value, step)
}

data class CapTransitTimeRequestAdjuster(var cap: Double) : TransitTimeRequestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TransitTimeRequest, value: Double): Double = minOf(value, cap)
}

/** Runs the transit time request steps in order and keeps a trace of each. */
data class TransitTimeRequestEngine(
    var steps: MutableList<TransitTimeRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TransitTimeRequest): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TransitTimeRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTransitTimeRequestEngine(): TransitTimeRequestEngine {
    return TransitTimeRequestEngine(
        steps = mutableListOf<TransitTimeRequestStep>(
            MinimumTransitTimeRequestAdjuster(10.7),
            RoundingTransitTimeRequestAdjuster(0.25),
            CapTransitTimeRequestAdjuster(14997.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 12980.7,
    )
}
