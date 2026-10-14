package com.acme.transport.crewing.swap.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted swap slot calculation. */
interface WeightedSwapSlotStep {
    fun describe(): String
    fun adjust(item: WeightedSwapSlot, value: Double): Double
}

data class CapWeightedSwapSlotAdjuster(var cap: Double) : WeightedSwapSlotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedSwapSlot, value: Double): Double = minOf(value, cap)
}

data class FlaggedWeightedSwapSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedSwapSlotStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedSwapSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameWeightedSwapSlotAdjuster(var factors: MutableMap<String, Double>) : WeightedSwapSlotStep {
    override fun describe(): String = "by name"

    override fun adjust(item: WeightedSwapSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class WeightedSwapSlotVersionOffsetAdjuster(var weight: Double) : WeightedSwapSlotStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: WeightedSwapSlot, value: Double): Double = value + item.version.toDouble() * weight
}

data class TieredWeightedSwapSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedSwapSlotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedSwapSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the weighted swap slot steps in order and keeps a trace of each. */
data class WeightedSwapSlotEngine(
    var steps: MutableList<WeightedSwapSlotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedSwapSlot): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedSwapSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedSwapSlotEngine(flags: Registry): WeightedSwapSlotEngine {
    return WeightedSwapSlotEngine(
        steps = mutableListOf<WeightedSwapSlotStep>(
            CapWeightedSwapSlotAdjuster(27112.0),
            FlaggedWeightedSwapSlotAdjuster(flags, "weighted-swap-slot-capped", 9.4),
            NameWeightedSwapSlotAdjuster(mutableMapOf<String, Double>("retail" to 1.34, "south" to 1.2)),
            WeightedSwapSlotVersionOffsetAdjuster(0.85),
            TieredWeightedSwapSlotAdjuster(
                mutableListOf<Double>(5133.0, 8810.0, 9055.0),
                mutableListOf<Double>(0.85, 1.0, 1.07),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 24920.8,
    )
}
