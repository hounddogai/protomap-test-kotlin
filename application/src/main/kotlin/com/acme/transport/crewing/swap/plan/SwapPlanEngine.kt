package com.acme.transport.crewing.swap.plan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the swap plan calculation. */
interface SwapPlanModifier {
    fun describe(): String
    fun adjust(item: SwapPlan, value: Double): Double
}

data class CapSwapPlanAdjuster(var cap: Double) : SwapPlanModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapPlan, value: Double): Double = minOf(value, cap)
}

data class MinimumSwapPlanAdjuster(var minimum: Double) : SwapPlanModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SwapPlanMarginOffsetAdjuster(var weight: Double) : SwapPlanModifier {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: SwapPlan, value: Double): Double = value + item.margin * weight
}

data class PercentSwapPlanAdjuster(var percent: Double) : SwapPlanModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingSwapPlanAdjuster(var step: Double) : SwapPlanModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapPlan, value: Double): Double = roundTo(value, step)
}

/** Runs the swap plan steps in order and keeps a trace of each. */
data class SwapPlanEngine(
    var steps: MutableList<SwapPlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SwapPlan): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSwapPlanEngine(): SwapPlanEngine {
    return SwapPlanEngine(
        steps = mutableListOf<SwapPlanModifier>(
            CapSwapPlanAdjuster(10816.0),
            MinimumSwapPlanAdjuster(39.0),
            SwapPlanMarginOffsetAdjuster(0.48),
            PercentSwapPlanAdjuster(20.3),
            RoundingSwapPlanAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 85563.5,
    )
}
