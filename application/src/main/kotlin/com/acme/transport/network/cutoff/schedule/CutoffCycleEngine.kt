package com.acme.transport.network.cutoff.schedule

import com.acme.transport.kit.numbers.roundTo

/** One step of the cutoff cycle calculation. */
interface CutoffCycleStep {
    fun describe(): String
    fun adjust(item: CutoffCycle, value: Double): Double
}

data class CapCutoffCycleAdjuster(var cap: Double) : CutoffCycleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CutoffCycle, value: Double): Double = minOf(value, cap)
}

data class MinimumCutoffCycleAdjuster(var minimum: Double) : CutoffCycleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CutoffCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCutoffCycleAdjuster(var step: Double) : CutoffCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CutoffCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the cutoff cycle steps in order and keeps a trace of each. */
data class CutoffCycleEngine(
    var steps: MutableList<CutoffCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CutoffCycle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CutoffCycleStep = steps[index]
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

    fun evaluateAll(items: MutableList<CutoffCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCutoffCycleEngine(): CutoffCycleEngine {
    return CutoffCycleEngine(
        steps = mutableListOf<CutoffCycleStep>(
            CapCutoffCycleAdjuster(42640.0),
            MinimumCutoffCycleAdjuster(2.0),
            RoundingCutoffCycleAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 94285.3,
    )
}
