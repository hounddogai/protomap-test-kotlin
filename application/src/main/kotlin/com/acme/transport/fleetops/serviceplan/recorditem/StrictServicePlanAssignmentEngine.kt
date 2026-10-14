package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict service plan assignment calculation. */
interface StrictServicePlanAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: StrictServicePlanAssignment, value: Double): Double
}

data class CapStrictServicePlanAssignmentAdjuster(var cap: Double) : StrictServicePlanAssignmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictServicePlanAssignment, value: Double): Double = minOf(value, cap)
}

data class PercentStrictServicePlanAssignmentAdjuster(
    var percent: Double,
) : StrictServicePlanAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictServicePlanAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleStrictServicePlanAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictServicePlanAssignmentAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictServicePlanAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict service plan assignment steps in order and keeps a trace of each. */
data class StrictServicePlanAssignmentEngine(
    var steps: MutableList<StrictServicePlanAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictServicePlanAssignment): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictServicePlanAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictServicePlanAssignmentEngine(): StrictServicePlanAssignmentEngine {
    return StrictServicePlanAssignmentEngine(
        steps = mutableListOf<StrictServicePlanAssignmentAdjuster>(
            CapStrictServicePlanAssignmentAdjuster(8618.0),
            PercentStrictServicePlanAssignmentAdjuster(-9.9),
            TitleStrictServicePlanAssignmentAdjuster(
                mutableMapOf<String, Double>("south" to 0.77, "east" to 0.74),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 49856.1,
    )
}
