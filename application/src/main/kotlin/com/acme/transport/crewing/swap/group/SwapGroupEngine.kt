package com.acme.transport.crewing.swap.group

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the swap group calculation. */
interface SwapGroupFactor {
    fun describe(): String
    fun adjust(item: SwapGroup, value: Double): Double
}

data class CategorySwapGroupAdjuster(var factors: MutableMap<String, Double>) : SwapGroupFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: SwapGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumSwapGroupAdjuster(var minimum: Double) : SwapGroupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SwapGroupEtaMinutesOffsetAdjuster(var weight: Double) : SwapGroupFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: SwapGroup, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class RoundingSwapGroupAdjuster(var step: Double) : SwapGroupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapGroup, value: Double): Double = roundTo(value, step)
}

data class PercentSwapGroupAdjuster(var percent: Double) : SwapGroupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the swap group steps in order and keeps a trace of each. */
data class SwapGroupEngine(
    var steps: MutableList<SwapGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SwapGroup): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSwapGroupEngine(): SwapGroupEngine {
    return SwapGroupEngine(
        steps = mutableListOf<SwapGroupFactor>(
            CategorySwapGroupAdjuster(
                mutableMapOf<String, Double>("north" to 0.77, "south" to 0.79, "central" to 1.0),
            ),
            MinimumSwapGroupAdjuster(8.6),
            SwapGroupEtaMinutesOffsetAdjuster(0.83),
            RoundingSwapGroupAdjuster(0.25),
            PercentSwapGroupAdjuster(24.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 71622.6,
    )
}
