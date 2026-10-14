package com.acme.transport.crewing.swap.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the swap override calculation. */
interface SwapOverrideFactor {
    fun describe(): String
    fun adjust(item: SwapOverride, value: Double): Double
}

data class DepotSwapOverrideAdjuster(var factors: MutableMap<String, Double>) : SwapOverrideFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: SwapOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapSwapOverrideAdjuster(var cap: Double) : SwapOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapOverride, value: Double): Double = minOf(value, cap)
}

data class SwapOverrideMaximumOffsetAdjuster(var weight: Double) : SwapOverrideFactor {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: SwapOverride, value: Double): Double = value + item.maximum * weight
}

data class RoundingSwapOverrideAdjuster(var step: Double) : SwapOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapOverride, value: Double): Double = roundTo(value, step)
}

data class FlaggedSwapOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the swap override steps in order and keeps a trace of each. */
data class SwapOverrideEngine(
    var steps: MutableList<SwapOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SwapOverride): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SwapOverrideFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SwapOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSwapOverrideEngine(flags: Registry): SwapOverrideEngine {
    return SwapOverrideEngine(
        steps = mutableListOf<SwapOverrideFactor>(
            DepotSwapOverrideAdjuster(mutableMapOf<String, Double>("south" to 0.88, "central" to 1.16)),
            CapSwapOverrideAdjuster(17886.0),
            SwapOverrideMaximumOffsetAdjuster(0.38),
            RoundingSwapOverrideAdjuster(5.0),
            FlaggedSwapOverrideAdjuster(flags, "swap-override-batched", 3.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 21837.2,
    )
}
