package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted violation cycle calculation. */
interface WeightedViolationCycleFactor {
    fun describe(): String
    fun adjust(item: WeightedViolationCycle, value: Double): Double
}

data class DepotWeightedViolationCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedViolationCycleFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: WeightedViolationCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumWeightedViolationCycleAdjuster(var minimum: Double) : WeightedViolationCycleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedViolationCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingWeightedViolationCycleAdjuster(var step: Double) : WeightedViolationCycleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedViolationCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted violation cycle steps in order and keeps a trace of each. */
data class WeightedViolationCycleEngine(
    var steps: MutableList<WeightedViolationCycleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedViolationCycle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedViolationCycleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedViolationCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedViolationCycleEngine(): WeightedViolationCycleEngine {
    return WeightedViolationCycleEngine(
        steps = mutableListOf<WeightedViolationCycleFactor>(
            DepotWeightedViolationCycleAdjuster(
                mutableMapOf<String, Double>("north" to 1.13, "south" to 1.3, "retail" to 1.02),
            ),
            MinimumWeightedViolationCycleAdjuster(48.4),
            RoundingWeightedViolationCycleAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 80297.3,
    )
}
