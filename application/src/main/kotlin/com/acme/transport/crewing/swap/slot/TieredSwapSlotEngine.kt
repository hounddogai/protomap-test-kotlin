package com.acme.transport.crewing.swap.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered swap slot calculation. */
interface TieredSwapSlotFactor {
    fun describe(): String
    fun adjust(item: TieredSwapSlot, value: Double): Double
}

data class CapTieredSwapSlotAdjuster(var cap: Double) : TieredSwapSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredSwapSlot, value: Double): Double = minOf(value, cap)
}

data class TieredTieredSwapSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredSwapSlotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredSwapSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedTieredSwapSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredSwapSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredSwapSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the tiered swap slot steps in order and keeps a trace of each. */
data class TieredSwapSlotEngine(
    var steps: MutableList<TieredSwapSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredSwapSlot): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredSwapSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredSwapSlotEngine(flags: Registry): TieredSwapSlotEngine {
    return TieredSwapSlotEngine(
        steps = mutableListOf<TieredSwapSlotFactor>(
            CapTieredSwapSlotAdjuster(39539.0),
            TieredTieredSwapSlotAdjuster(
                mutableListOf<Double>(4107.0, 5862.0, 7097.0, 8671.0),
                mutableListOf<Double>(1.12, 0.92, 1.14, 0.94),
            ),
            FlaggedTieredSwapSlotAdjuster(flags, "tiered-swap-slot-global", 2.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 35117.7,
    )
}
