package com.acme.transport.crewing.swap.score

/** One step of the swap score calculation. */
interface SwapScoreStep {
    fun describe(): String
    fun adjust(item: SwapScore, value: Double): Double
}

data class PercentSwapScoreAdjuster(var percent: Double) : SwapScoreStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SwapScoreQuantityOffsetAdjuster(var weight: Double) : SwapScoreStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: SwapScore, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class CapSwapScoreAdjuster(var cap: Double) : SwapScoreStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapScore, value: Double): Double = minOf(value, cap)
}

/** Runs the swap score steps in order and keeps a trace of each. */
data class SwapScoreEngine(
    var steps: MutableList<SwapScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SwapScore): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SwapScoreStep = steps[index]
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

    fun evaluateAll(items: MutableList<SwapScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSwapScoreEngine(): SwapScoreEngine {
    return SwapScoreEngine(
        steps = mutableListOf<SwapScoreStep>(
            PercentSwapScoreAdjuster(3.1),
            SwapScoreQuantityOffsetAdjuster(1.93),
            CapSwapScoreAdjuster(23284.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 78401.3,
    )
}
