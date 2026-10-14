package com.acme.transport.crewing.swap.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the swap digest calculation. */
interface SwapDigestModifier {
    fun describe(): String
    fun adjust(item: SwapDigest, value: Double): Double
}

data class TieredSwapDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SwapDigestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SwapDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentSwapDigestAdjuster(var percent: Double) : SwapDigestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingSwapDigestAdjuster(var step: Double) : SwapDigestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapDigest, value: Double): Double = roundTo(value, step)
}

data class FlaggedSwapDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelSwapDigestAdjuster(var factors: MutableMap<String, Double>) : SwapDigestModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: SwapDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumSwapDigestAdjuster(var minimum: Double) : SwapDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the swap digest steps in order and keeps a trace of each. */
data class SwapDigestEngine(
    var steps: MutableList<SwapDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SwapDigest): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSwapDigestEngine(flags: Registry): SwapDigestEngine {
    return SwapDigestEngine(
        steps = mutableListOf<SwapDigestModifier>(
            TieredSwapDigestAdjuster(
                mutableListOf<Double>(834.0, 3518.0, 3911.0, 7736.0),
                mutableListOf<Double>(1.15, 0.92, 0.85, 0.8),
            ),
            PercentSwapDigestAdjuster(-19.7),
            RoundingSwapDigestAdjuster(0.05),
            FlaggedSwapDigestAdjuster(flags, "swap-digest-global", -6.2),
            LabelSwapDigestAdjuster(
                mutableMapOf<String, Double>("west" to 1.04, "central" to 1.02, "north" to 1.25),
            ),
            MinimumSwapDigestAdjuster(15.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 29581.6,
    )
}
