package com.acme.transport.crewing.swap.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the swap claim calculation. */
interface SwapClaimAdjuster {
    fun describe(): String
    fun adjust(item: SwapClaim, value: Double): Double
}

data class FlaggedSwapClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapClaimAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentSwapClaimAdjuster(var percent: Double) : SwapClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapSwapClaimAdjuster(var cap: Double) : SwapClaimAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapClaim, value: Double): Double = minOf(value, cap)
}

data class SegmentSwapClaimAdjuster(var factors: MutableMap<String, Double>) : SwapClaimAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: SwapClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSwapClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SwapClaimAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SwapClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the swap claim steps in order and keeps a trace of each. */
data class SwapClaimEngine(
    var steps: MutableList<SwapClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SwapClaim): Double {
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

    fun evaluateAll(items: MutableList<SwapClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSwapClaimEngine(flags: Registry): SwapClaimEngine {
    return SwapClaimEngine(
        steps = mutableListOf<SwapClaimAdjuster>(
            FlaggedSwapClaimAdjuster(flags, "swap-claim-weighted", -1.9),
            PercentSwapClaimAdjuster(20.3),
            CapSwapClaimAdjuster(11560.0),
            SegmentSwapClaimAdjuster(mutableMapOf<String, Double>("east" to 0.76, "central" to 0.75)),
            TieredSwapClaimAdjuster(
                mutableListOf<Double>(3046.0, 5874.0, 8321.0),
                mutableListOf<Double>(0.99, 0.83, 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 41266.8,
    )
}
