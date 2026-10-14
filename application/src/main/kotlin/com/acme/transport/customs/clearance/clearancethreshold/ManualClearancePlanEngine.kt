package com.acme.transport.customs.clearance.clearancethreshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual clearance plan calculation. */
interface ManualClearancePlanFactor {
    fun describe(): String
    fun adjust(item: ManualClearancePlan, value: Double): Double
}

data class NameManualClearancePlanAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualClearancePlanFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: ManualClearancePlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingManualClearancePlanAdjuster(var step: Double) : ManualClearancePlanFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualClearancePlan, value: Double): Double = roundTo(value, step)
}

data class MinimumManualClearancePlanAdjuster(var minimum: Double) : ManualClearancePlanFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualClearancePlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapManualClearancePlanAdjuster(var cap: Double) : ManualClearancePlanFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualClearancePlan, value: Double): Double = minOf(value, cap)
}

/** Runs the manual clearance plan steps in order and keeps a trace of each. */
data class ManualClearancePlanEngine(
    var steps: MutableList<ManualClearancePlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualClearancePlan): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualClearancePlanFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualClearancePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualClearancePlanEngine(): ManualClearancePlanEngine {
    return ManualClearancePlanEngine(
        steps = mutableListOf<ManualClearancePlanFactor>(
            NameManualClearancePlanAdjuster(mutableMapOf<String, Double>("retail" to 1.0)),
            RoundingManualClearancePlanAdjuster(0.25),
            MinimumManualClearancePlanAdjuster(28.5),
            CapManualClearancePlanAdjuster(9306.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 89011.2,
    )
}
