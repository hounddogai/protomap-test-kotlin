package com.acme.transport.crewing.swap.audit

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the swap milestone calculation. */
interface SwapMilestoneStep {
    fun describe(): String
    fun adjust(item: SwapMilestone, value: Double): Double
}

data class LabelSwapMilestoneAdjuster(var factors: MutableMap<String, Double>) : SwapMilestoneStep {
    override fun describe(): String = "by label"

    override fun adjust(item: SwapMilestone, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapSwapMilestoneAdjuster(var cap: Double) : SwapMilestoneStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapMilestone, value: Double): Double = minOf(value, cap)
}

data class MinimumSwapMilestoneAdjuster(var minimum: Double) : SwapMilestoneStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapMilestone, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSwapMilestoneAdjuster(var step: Double) : SwapMilestoneStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapMilestone, value: Double): Double = roundTo(value, step)
}

data class SwapMilestoneCostOffsetAdjuster(var weight: Double) : SwapMilestoneStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: SwapMilestone, value: Double): Double = value + item.cost * weight
}

/** Runs the swap milestone steps in order and keeps a trace of each. */
data class SwapMilestoneEngine(
    var steps: MutableList<SwapMilestoneStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SwapMilestone): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapMilestone>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSwapMilestoneEngine(): SwapMilestoneEngine {
    return SwapMilestoneEngine(
        steps = mutableListOf<SwapMilestoneStep>(
            LabelSwapMilestoneAdjuster(mutableMapOf<String, Double>("west" to 0.85)),
            CapSwapMilestoneAdjuster(23967.0),
            MinimumSwapMilestoneAdjuster(42.9),
            RoundingSwapMilestoneAdjuster(0.05),
            SwapMilestoneCostOffsetAdjuster(0.87),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 70508.3,
    )
}
