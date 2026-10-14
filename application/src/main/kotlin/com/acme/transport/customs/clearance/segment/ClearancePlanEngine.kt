package com.acme.transport.customs.clearance.segment

import com.acme.transport.kit.numbers.roundTo

/** One step of the clearance plan calculation. */
interface ClearancePlanAdjuster {
    fun describe(): String
    fun adjust(item: ClearancePlan, value: Double): Double
}

data class PercentClearancePlanAdjuster(var percent: Double) : ClearancePlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearancePlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class DepotClearancePlanAdjuster(var factors: MutableMap<String, Double>) : ClearancePlanAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: ClearancePlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumClearancePlanAdjuster(var minimum: Double) : ClearancePlanAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearancePlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapClearancePlanAdjuster(var cap: Double) : ClearancePlanAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearancePlan, value: Double): Double = minOf(value, cap)
}

data class RoundingClearancePlanAdjuster(var step: Double) : ClearancePlanAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearancePlan, value: Double): Double = roundTo(value, step)
}

/** Runs the clearance plan steps in order and keeps a trace of each. */
data class ClearancePlanEngine(
    var steps: MutableList<ClearancePlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ClearancePlan): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearancePlanAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ClearancePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultClearancePlanEngine(): ClearancePlanEngine {
    return ClearancePlanEngine(
        steps = mutableListOf<ClearancePlanAdjuster>(
            PercentClearancePlanAdjuster(24.1),
            DepotClearancePlanAdjuster(mutableMapOf<String, Double>("west" to 0.78, "retail" to 1.13)),
            MinimumClearancePlanAdjuster(13.1),
            CapClearancePlanAdjuster(3637.0),
            RoundingClearancePlanAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 74840.5,
    )
}
