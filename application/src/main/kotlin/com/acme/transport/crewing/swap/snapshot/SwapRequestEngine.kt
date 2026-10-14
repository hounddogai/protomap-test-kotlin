package com.acme.transport.crewing.swap.snapshot

import com.acme.transport.kit.flags.Registry

/** One step of the swap request calculation. */
interface SwapRequestModifier {
    fun describe(): String
    fun adjust(item: SwapRequest, value: Double): Double
}

data class FlaggedSwapRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapRequestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class ChannelSwapRequestAdjuster(var factors: MutableMap<String, Double>) : SwapRequestModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: SwapRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentSwapRequestAdjuster(var percent: Double) : SwapRequestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumSwapRequestAdjuster(var minimum: Double) : SwapRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredSwapRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SwapRequestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SwapRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SwapRequestWeightOffsetAdjuster(var weight: Double) : SwapRequestModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: SwapRequest, value: Double): Double = value + item.weight * weight
}

/** Runs the swap request steps in order and keeps a trace of each. */
data class SwapRequestEngine(
    var steps: MutableList<SwapRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SwapRequest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SwapRequestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SwapRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSwapRequestEngine(flags: Registry): SwapRequestEngine {
    return SwapRequestEngine(
        steps = mutableListOf<SwapRequestModifier>(
            FlaggedSwapRequestAdjuster(flags, "swap-request-seasonal", 0.8),
            ChannelSwapRequestAdjuster(
                mutableMapOf<String, Double>("online" to 1.2, "retail" to 0.89, "east" to 0.78),
            ),
            PercentSwapRequestAdjuster(-3.3),
            MinimumSwapRequestAdjuster(29.0),
            TieredSwapRequestAdjuster(
                mutableListOf<Double>(51.0, 190.0, 7830.0, 8302.0),
                mutableListOf<Double>(1.08, 0.93, 1.11, 0.81),
            ),
            SwapRequestWeightOffsetAdjuster(0.22),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 79081.2,
    )
}
