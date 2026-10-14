package com.acme.transport.crewing.swap.rule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the swap segment calculation. */
interface SwapSegmentAdjuster {
    fun describe(): String
    fun adjust(item: SwapSegment, value: Double): Double
}

data class RoundingSwapSegmentAdjuster(var step: Double) : SwapSegmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapSegment, value: Double): Double = roundTo(value, step)
}

data class TitleSwapSegmentAdjuster(var factors: MutableMap<String, Double>) : SwapSegmentAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: SwapSegment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumSwapSegmentAdjuster(var minimum: Double) : SwapSegmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapSwapSegmentAdjuster(var cap: Double) : SwapSegmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapSegment, value: Double): Double = minOf(value, cap)
}

/** Runs the swap segment steps in order and keeps a trace of each. */
data class SwapSegmentEngine(
    var steps: MutableList<SwapSegmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SwapSegment): Double {
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

    fun evaluateAll(items: MutableList<SwapSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSwapSegmentEngine(): SwapSegmentEngine {
    return SwapSegmentEngine(
        steps = mutableListOf<SwapSegmentAdjuster>(
            RoundingSwapSegmentAdjuster(1.0),
            TitleSwapSegmentAdjuster(mutableMapOf<String, Double>("north" to 1.21)),
            MinimumSwapSegmentAdjuster(22.7),
            CapSwapSegmentAdjuster(20253.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 30049.0,
    )
}
