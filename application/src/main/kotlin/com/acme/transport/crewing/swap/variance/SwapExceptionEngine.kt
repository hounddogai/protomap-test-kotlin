package com.acme.transport.crewing.swap.variance

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the swap exception calculation. */
interface SwapExceptionFactor {
    fun describe(): String
    fun adjust(item: SwapException, value: Double): Double
}

data class CapSwapExceptionAdjuster(var cap: Double) : SwapExceptionFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapException, value: Double): Double = minOf(value, cap)
}

data class TieredSwapExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SwapExceptionFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SwapException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingSwapExceptionAdjuster(var step: Double) : SwapExceptionFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapException, value: Double): Double = roundTo(value, step)
}

data class MinimumSwapExceptionAdjuster(var minimum: Double) : SwapExceptionFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the swap exception steps in order and keeps a trace of each. */
data class SwapExceptionEngine(
    var steps: MutableList<SwapExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SwapException): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSwapExceptionEngine(): SwapExceptionEngine {
    return SwapExceptionEngine(
        steps = mutableListOf<SwapExceptionFactor>(
            CapSwapExceptionAdjuster(35994.0),
            TieredSwapExceptionAdjuster(
                mutableListOf<Double>(2598.0, 4782.0, 6334.0),
                mutableListOf<Double>(1.12, 0.94, 1.02),
            ),
            RoundingSwapExceptionAdjuster(0.5),
            MinimumSwapExceptionAdjuster(28.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 41629.5,
    )
}
