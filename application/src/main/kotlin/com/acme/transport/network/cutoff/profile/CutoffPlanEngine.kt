package com.acme.transport.network.cutoff.profile

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cutoff plan calculation. */
interface CutoffPlanStep {
    fun describe(): String
    fun adjust(item: CutoffPlan, value: Double): Double
}

data class SourceCutoffPlanAdjuster(var factors: MutableMap<String, Double>) : CutoffPlanStep {
    override fun describe(): String = "by source"

    override fun adjust(item: CutoffPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCutoffPlanAdjuster(var minimum: Double) : CutoffPlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CutoffPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CutoffPlanBudgetOffsetAdjuster(var weight: Double) : CutoffPlanStep {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: CutoffPlan, value: Double): Double = value + item.budget * weight
}

data class CapCutoffPlanAdjuster(var cap: Double) : CutoffPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffPlan, value: Double): Double = minOf(value, cap)
}

data class RoundingCutoffPlanAdjuster(var step: Double) : CutoffPlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffPlan, value: Double): Double = roundTo(value, step)
}

data class PercentCutoffPlanAdjuster(var percent: Double) : CutoffPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CutoffPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the cutoff plan steps in order and keeps a trace of each. */
data class CutoffPlanEngine(
    var steps: MutableList<CutoffPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CutoffPlan): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CutoffPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCutoffPlanEngine(): CutoffPlanEngine {
    return CutoffPlanEngine(
        steps = mutableListOf<CutoffPlanStep>(
            SourceCutoffPlanAdjuster(mutableMapOf<String, Double>("online" to 1.27)),
            MinimumCutoffPlanAdjuster(16.4),
            CutoffPlanBudgetOffsetAdjuster(1.3),
            CapCutoffPlanAdjuster(27945.0),
            RoundingCutoffPlanAdjuster(0.05),
            PercentCutoffPlanAdjuster(-9.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 23756.5,
    )
}
