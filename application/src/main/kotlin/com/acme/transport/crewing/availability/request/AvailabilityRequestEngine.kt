package com.acme.transport.crewing.availability.request

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the availability request calculation. */
interface AvailabilityRequestModifier {
    fun describe(): String
    fun adjust(item: AvailabilityRequest, value: Double): Double
}

data class AvailabilityRequestStopCountOffsetAdjuster(var weight: Double) : AvailabilityRequestModifier {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: AvailabilityRequest, value: Double): Double = value + item.stopCount.toDouble() * weight
}

data class CapAvailabilityRequestAdjuster(var cap: Double) : AvailabilityRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AvailabilityRequest, value: Double): Double = minOf(value, cap)
}

data class PercentAvailabilityRequestAdjuster(var percent: Double) : AvailabilityRequestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AvailabilityRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAvailabilityRequestAdjuster(var minimum: Double) : AvailabilityRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the availability request steps in order and keeps a trace of each. */
data class AvailabilityRequestEngine(
    var steps: MutableList<AvailabilityRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AvailabilityRequest): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AvailabilityRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAvailabilityRequestEngine(): AvailabilityRequestEngine {
    return AvailabilityRequestEngine(
        steps = mutableListOf<AvailabilityRequestModifier>(
            AvailabilityRequestStopCountOffsetAdjuster(1.99),
            CapAvailabilityRequestAdjuster(20162.0),
            PercentAvailabilityRequestAdjuster(0.3),
            MinimumAvailabilityRequestAdjuster(31.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 61135.0,
    )
}
