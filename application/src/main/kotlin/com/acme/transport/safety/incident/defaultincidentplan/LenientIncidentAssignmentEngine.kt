package com.acme.transport.safety.incident.defaultincidentplan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient incident assignment calculation. */
interface LenientIncidentAssignmentStep {
    fun describe(): String
    fun adjust(item: LenientIncidentAssignment, value: Double): Double
}

data class CapLenientIncidentAssignmentAdjuster(var cap: Double) : LenientIncidentAssignmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientIncidentAssignment, value: Double): Double = minOf(value, cap)
}

data class RoundingLenientIncidentAssignmentAdjuster(var step: Double) : LenientIncidentAssignmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientIncidentAssignment, value: Double): Double = roundTo(value, step)
}

data class PercentLenientIncidentAssignmentAdjuster(var percent: Double) : LenientIncidentAssignmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientIncidentAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLenientIncidentAssignmentAdjuster(var minimum: Double) : LenientIncidentAssignmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientIncidentAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient incident assignment steps in order and keeps a trace of each. */
data class LenientIncidentAssignmentEngine(
    var steps: MutableList<LenientIncidentAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientIncidentAssignment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientIncidentAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientIncidentAssignmentEngine(): LenientIncidentAssignmentEngine {
    return LenientIncidentAssignmentEngine(
        steps = mutableListOf<LenientIncidentAssignmentStep>(
            CapLenientIncidentAssignmentAdjuster(25101.0),
            RoundingLenientIncidentAssignmentAdjuster(1.0),
            PercentLenientIncidentAssignmentAdjuster(-17.8),
            MinimumLenientIncidentAssignmentAdjuster(7.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 85176.4,
    )
}
