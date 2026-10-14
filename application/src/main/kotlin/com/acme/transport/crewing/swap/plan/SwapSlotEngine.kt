package com.acme.transport.crewing.swap.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the swap slot calculation. */
interface SwapSlotModifier {
    fun describe(): String
    fun adjust(item: SwapSlot, value: Double): Double
}

data class CapSwapSlotAdjuster(var cap: Double) : SwapSlotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SwapSlot, value: Double): Double = minOf(value, cap)
}

data class SwapSlotCreatedAtOffsetAdjuster(var weight: Double) : SwapSlotModifier {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: SwapSlot, value: Double): Double = value + item.createdAt.toDouble() * weight
}

data class FlaggedSwapSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapSlotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSwapSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SwapSlotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SwapSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the swap slot steps in order and keeps a trace of each. */
data class SwapSlotEngine(
    var steps: MutableList<SwapSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SwapSlot): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSwapSlotEngine(flags: Registry): SwapSlotEngine {
    return SwapSlotEngine(
        steps = mutableListOf<SwapSlotModifier>(
            CapSwapSlotAdjuster(17510.0),
            SwapSlotCreatedAtOffsetAdjuster(1.36),
            FlaggedSwapSlotAdjuster(flags, "swap-slot-adaptive", -1.2),
            TieredSwapSlotAdjuster(
                mutableListOf<Double>(3656.0, 6714.0, 7385.0),
                mutableListOf<Double>(0.96, 0.95, 1.0),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 93921.6,
    )
}
