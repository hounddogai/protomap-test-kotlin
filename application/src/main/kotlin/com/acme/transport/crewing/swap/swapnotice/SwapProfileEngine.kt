package com.acme.transport.crewing.swap.swapnotice

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the swap profile calculation. */
interface SwapProfileStep {
    fun describe(): String
    fun adjust(item: SwapProfile, value: Double): Double
}

data class FlaggedSwapProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapProfileStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SwapProfileBudgetOffsetAdjuster(var weight: Double) : SwapProfileStep {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: SwapProfile, value: Double): Double = value + item.budget * weight
}

data class NameSwapProfileAdjuster(var factors: MutableMap<String, Double>) : SwapProfileStep {
    override fun describe(): String = "by name"

    override fun adjust(item: SwapProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentSwapProfileAdjuster(var percent: Double) : SwapProfileStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapSwapProfileAdjuster(var cap: Double) : SwapProfileStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapProfile, value: Double): Double = minOf(value, cap)
}

/** Runs the swap profile steps in order and keeps a trace of each. */
data class SwapProfileEngine(
    var steps: MutableList<SwapProfileStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SwapProfile): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSwapProfileEngine(flags: Registry): SwapProfileEngine {
    return SwapProfileEngine(
        steps = mutableListOf<SwapProfileStep>(
            FlaggedSwapProfileAdjuster(flags, "swap-profile-lenient", 0.0),
            SwapProfileBudgetOffsetAdjuster(1.59),
            NameSwapProfileAdjuster(
                mutableMapOf<String, Double>("south" to 1.34, "online" to 0.7, "east" to 0.87),
            ),
            PercentSwapProfileAdjuster(3.0),
            CapSwapProfileAdjuster(28691.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 98564.9,
    )
}
