package com.acme.transport.crewing.swap.variance

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the swap estimate calculation. */
interface SwapEstimateAdjuster {
    fun describe(): String
    fun adjust(item: SwapEstimate, value: Double): Double
}

data class PercentSwapEstimateAdjuster(var percent: Double) : SwapEstimateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedSwapEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapEstimateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingSwapEstimateAdjuster(var step: Double) : SwapEstimateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapEstimate, value: Double): Double = roundTo(value, step)
}

/** Runs the swap estimate steps in order and keeps a trace of each. */
data class SwapEstimateEngine(
    var steps: MutableList<SwapEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SwapEstimate): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SwapEstimateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SwapEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSwapEstimateEngine(flags: Registry): SwapEstimateEngine {
    return SwapEstimateEngine(
        steps = mutableListOf<SwapEstimateAdjuster>(
            PercentSwapEstimateAdjuster(-8.5),
            FlaggedSwapEstimateAdjuster(flags, "swap-estimate-capped", -7.7),
            RoundingSwapEstimateAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 54417.8,
    )
}
