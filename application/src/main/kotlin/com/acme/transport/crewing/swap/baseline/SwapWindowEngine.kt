package com.acme.transport.crewing.swap.baseline

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the swap window calculation. */
interface SwapWindowFactor {
    fun describe(): String
    fun adjust(item: SwapWindow, value: Double): Double
}

data class RegionSwapWindowAdjuster(var factors: MutableMap<String, Double>) : SwapWindowFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: SwapWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingSwapWindowAdjuster(var step: Double) : SwapWindowFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapWindow, value: Double): Double = roundTo(value, step)
}

data class MinimumSwapWindowAdjuster(var minimum: Double) : SwapWindowFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapSwapWindowAdjuster(var cap: Double) : SwapWindowFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapWindow, value: Double): Double = minOf(value, cap)
}

data class SwapWindowDurationMinutesOffsetAdjuster(var weight: Double) : SwapWindowFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: SwapWindow, value: Double): Double = value + item.durationMinutes.toDouble() * weight
}

data class PercentSwapWindowAdjuster(var percent: Double) : SwapWindowFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the swap window steps in order and keeps a trace of each. */
data class SwapWindowEngine(
    var steps: MutableList<SwapWindowFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SwapWindow): Double {
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

    fun evaluateAll(items: MutableList<SwapWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSwapWindowEngine(): SwapWindowEngine {
    return SwapWindowEngine(
        steps = mutableListOf<SwapWindowFactor>(
            RegionSwapWindowAdjuster(mutableMapOf<String, Double>("west" to 1.27)),
            RoundingSwapWindowAdjuster(0.05),
            MinimumSwapWindowAdjuster(5.7),
            CapSwapWindowAdjuster(47910.0),
            SwapWindowDurationMinutesOffsetAdjuster(1.3),
            PercentSwapWindowAdjuster(-5.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.8,
        ceiling = 98777.9,
    )
}
