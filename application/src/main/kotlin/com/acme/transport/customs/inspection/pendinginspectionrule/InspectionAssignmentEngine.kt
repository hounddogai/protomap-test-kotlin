package com.acme.transport.customs.inspection.pendinginspectionrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection assignment calculation. */
interface InspectionAssignmentFactor {
    fun describe(): String
    fun adjust(item: InspectionAssignment, value: Double): Double
}

data class FlaggedInspectionAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionAssignmentFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapInspectionAssignmentAdjuster(var cap: Double) : InspectionAssignmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionAssignment, value: Double): Double = minOf(value, cap)
}

data class TieredInspectionAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionAssignmentFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumInspectionAssignmentAdjuster(var minimum: Double) : InspectionAssignmentFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class InspectionAssignmentScoreOffsetAdjuster(var weight: Double) : InspectionAssignmentFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: InspectionAssignment, value: Double): Double = value + item.score * weight
}

data class RoundingInspectionAssignmentAdjuster(var step: Double) : InspectionAssignmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionAssignment, value: Double): Double = roundTo(value, step)
}

/** Runs the inspection assignment steps in order and keeps a trace of each. */
data class InspectionAssignmentEngine(
    var steps: MutableList<InspectionAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: InspectionAssignment): Double {
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

    fun evaluateAll(items: MutableList<InspectionAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultInspectionAssignmentEngine(flags: Registry): InspectionAssignmentEngine {
    return InspectionAssignmentEngine(
        steps = mutableListOf<InspectionAssignmentFactor>(
            FlaggedInspectionAssignmentAdjuster(flags, "inspection-assignment-batched", 8.0),
            CapInspectionAssignmentAdjuster(2349.0),
            TieredInspectionAssignmentAdjuster(
                mutableListOf<Double>(1557.0, 3478.0, 4109.0, 9560.0),
                mutableListOf<Double>(0.86, 0.98, 0.87, 1.14),
            ),
            MinimumInspectionAssignmentAdjuster(47.4),
            InspectionAssignmentScoreOffsetAdjuster(1.8),
            RoundingInspectionAssignmentAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 3524.8,
    )
}
