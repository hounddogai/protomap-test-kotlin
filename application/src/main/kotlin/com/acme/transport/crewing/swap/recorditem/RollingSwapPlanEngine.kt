package com.acme.transport.crewing.swap.recorditem

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling swap plan calculation. */
interface RollingSwapPlanFactor {
    fun describe(): String
    fun adjust(item: RollingSwapPlan, value: Double): Double
}

data class CapRollingSwapPlanAdjuster(var cap: Double) : RollingSwapPlanFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingSwapPlan, value: Double): Double = minOf(value, cap)
}

data class NameRollingSwapPlanAdjuster(var factors: MutableMap<String, Double>) : RollingSwapPlanFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: RollingSwapPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRollingSwapPlanAdjuster(var step: Double) : RollingSwapPlanFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingSwapPlan, value: Double): Double = roundTo(value, step)
}

/** Runs the rolling swap plan steps in order and keeps a trace of each. */
data class RollingSwapPlanEngine(
    var steps: MutableList<RollingSwapPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingSwapPlan): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingSwapPlanFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingSwapPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingSwapPlanEngine(): RollingSwapPlanEngine {
    return RollingSwapPlanEngine(
        steps = mutableListOf<RollingSwapPlanFactor>(
            CapRollingSwapPlanAdjuster(5235.0),
            NameRollingSwapPlanAdjuster(
                mutableMapOf<String, Double>("online" to 0.85, "south" to 0.92, "east" to 0.74),
            ),
            RoundingRollingSwapPlanAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 39757.9,
    )
}
