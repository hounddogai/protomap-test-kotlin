package com.acme.transport.crewing.swap.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the swap entry calculation. */
interface SwapEntryAdjuster {
    fun describe(): String
    fun adjust(item: SwapEntry, value: Double): Double
}

data class NameSwapEntryAdjuster(var factors: MutableMap<String, Double>) : SwapEntryAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: SwapEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class SwapEntryFuelLitersOffsetAdjuster(var weight: Double) : SwapEntryAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: SwapEntry, value: Double): Double = value + item.fuelLiters * weight
}

data class RoundingSwapEntryAdjuster(var step: Double) : SwapEntryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SwapEntry, value: Double): Double = roundTo(value, step)
}

data class PercentSwapEntryAdjuster(var percent: Double) : SwapEntryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SwapEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedSwapEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapEntryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the swap entry steps in order and keeps a trace of each. */
data class SwapEntryEngine(
    var steps: MutableList<SwapEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SwapEntry): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SwapEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSwapEntryEngine(flags: Registry): SwapEntryEngine {
    return SwapEntryEngine(
        steps = mutableListOf<SwapEntryAdjuster>(
            NameSwapEntryAdjuster(mutableMapOf<String, Double>("retail" to 1.17, "north" to 1.35)),
            SwapEntryFuelLitersOffsetAdjuster(0.86),
            RoundingSwapEntryAdjuster(5.0),
            PercentSwapEntryAdjuster(16.2),
            FlaggedSwapEntryAdjuster(flags, "swap-entry-weighted", -4.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 15805.3,
    )
}
