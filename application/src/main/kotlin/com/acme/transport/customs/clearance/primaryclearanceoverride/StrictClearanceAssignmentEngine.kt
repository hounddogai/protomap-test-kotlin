package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict clearance assignment calculation. */
interface StrictClearanceAssignmentFactor {
    fun describe(): String
    fun adjust(item: StrictClearanceAssignment, value: Double): Double
}

data class PercentStrictClearanceAssignmentAdjuster(var percent: Double) : StrictClearanceAssignmentFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictClearanceAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapStrictClearanceAssignmentAdjuster(var cap: Double) : StrictClearanceAssignmentFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictClearanceAssignment, value: Double): Double = minOf(value, cap)
}

data class LabelStrictClearanceAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictClearanceAssignmentFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: StrictClearanceAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingStrictClearanceAssignmentAdjuster(var step: Double) : StrictClearanceAssignmentFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictClearanceAssignment, value: Double): Double = roundTo(value, step)
}

/** Runs the strict clearance assignment steps in order and keeps a trace of each. */
data class StrictClearanceAssignmentEngine(
    var steps: MutableList<StrictClearanceAssignmentFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictClearanceAssignment): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictClearanceAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictClearanceAssignmentEngine(): StrictClearanceAssignmentEngine {
    return StrictClearanceAssignmentEngine(
        steps = mutableListOf<StrictClearanceAssignmentFactor>(
            PercentStrictClearanceAssignmentAdjuster(5.0),
            CapStrictClearanceAssignmentAdjuster(2159.0),
            LabelStrictClearanceAssignmentAdjuster(mutableMapOf<String, Double>("south" to 0.73)),
            RoundingStrictClearanceAssignmentAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 10771.8,
    )
}
