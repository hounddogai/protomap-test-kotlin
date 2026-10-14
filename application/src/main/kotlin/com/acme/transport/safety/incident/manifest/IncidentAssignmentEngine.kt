package com.acme.transport.safety.incident.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the incident assignment calculation. */
interface IncidentAssignmentModifier {
    fun describe(): String
    fun adjust(item: IncidentAssignment, value: Double): Double
}

data class TieredIncidentAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : IncidentAssignmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: IncidentAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapIncidentAssignmentAdjuster(var cap: Double) : IncidentAssignmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IncidentAssignment, value: Double): Double = minOf(value, cap)
}

data class IncidentAssignmentMaximumOffsetAdjuster(var weight: Double) : IncidentAssignmentModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: IncidentAssignment, value: Double): Double = value + item.maximum * weight
}

data class FlaggedIncidentAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IncidentAssignmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IncidentAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingIncidentAssignmentAdjuster(var step: Double) : IncidentAssignmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IncidentAssignment, value: Double): Double = roundTo(value, step)
}

data class MinimumIncidentAssignmentAdjuster(var minimum: Double) : IncidentAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IncidentAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the incident assignment steps in order and keeps a trace of each. */
data class IncidentAssignmentEngine(
    var steps: MutableList<IncidentAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: IncidentAssignment): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IncidentAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultIncidentAssignmentEngine(flags: Registry): IncidentAssignmentEngine {
    return IncidentAssignmentEngine(
        steps = mutableListOf<IncidentAssignmentModifier>(
            TieredIncidentAssignmentAdjuster(
                mutableListOf<Double>(2884.0, 3675.0, 9847.0),
                mutableListOf<Double>(0.9, 1.15, 1.2),
            ),
            CapIncidentAssignmentAdjuster(12226.0),
            IncidentAssignmentMaximumOffsetAdjuster(1.56),
            FlaggedIncidentAssignmentAdjuster(flags, "incident-assignment-seasonal", 3.5),
            RoundingIncidentAssignmentAdjuster(0.25),
            MinimumIncidentAssignmentAdjuster(6.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 87907.0,
    )
}
