package com.acme.transport.crewing.swap.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the swap threshold calculation. */
interface SwapThresholdAdjuster {
    fun describe(): String
    fun adjust(item: SwapThreshold, value: Double): Double
}

data class CapSwapThresholdAdjuster(var cap: Double) : SwapThresholdAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapThreshold, value: Double): Double = minOf(value, cap)
}

data class ChannelSwapThresholdAdjuster(var factors: MutableMap<String, Double>) : SwapThresholdAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: SwapThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumSwapThresholdAdjuster(var minimum: Double) : SwapThresholdAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSwapThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapThresholdAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentSwapThresholdAdjuster(var percent: Double) : SwapThresholdAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the swap threshold steps in order and keeps a trace of each. */
data class SwapThresholdEngine(
    var steps: MutableList<SwapThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SwapThreshold): Double {
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

    fun evaluateAll(items: MutableList<SwapThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSwapThresholdEngine(flags: Registry): SwapThresholdEngine {
    return SwapThresholdEngine(
        steps = mutableListOf<SwapThresholdAdjuster>(
            CapSwapThresholdAdjuster(39026.0),
            ChannelSwapThresholdAdjuster(mutableMapOf<String, Double>("west" to 0.84)),
            MinimumSwapThresholdAdjuster(49.7),
            FlaggedSwapThresholdAdjuster(flags, "swap-threshold-primary", -9.3),
            PercentSwapThresholdAdjuster(-23.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 76146.5,
    )
}
