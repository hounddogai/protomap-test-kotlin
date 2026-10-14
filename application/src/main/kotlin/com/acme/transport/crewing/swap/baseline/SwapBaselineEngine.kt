package com.acme.transport.crewing.swap.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the swap baseline calculation. */
interface SwapBaselineFactor {
    fun describe(): String
    fun adjust(item: SwapBaseline, value: Double): Double
}

data class LocaleSwapBaselineAdjuster(var factors: MutableMap<String, Double>) : SwapBaselineFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: SwapBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumSwapBaselineAdjuster(var minimum: Double) : SwapBaselineFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSwapBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapBaselineFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the swap baseline steps in order and keeps a trace of each. */
data class SwapBaselineEngine(
    var steps: MutableList<SwapBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SwapBaseline): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSwapBaselineEngine(flags: Registry): SwapBaselineEngine {
    return SwapBaselineEngine(
        steps = mutableListOf<SwapBaselineFactor>(
            LocaleSwapBaselineAdjuster(
                mutableMapOf<String, Double>("online" to 1.3, "west" to 1.38, "central" to 0.72),
            ),
            MinimumSwapBaselineAdjuster(45.8),
            FlaggedSwapBaselineAdjuster(flags, "swap-baseline-rolling", 4.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 41614.0,
    )
}
