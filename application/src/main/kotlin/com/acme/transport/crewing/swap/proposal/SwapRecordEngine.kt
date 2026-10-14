package com.acme.transport.crewing.swap.proposal

import com.acme.transport.kit.flags.Registry

/** One step of the swap record calculation. */
interface SwapRecordFactor {
    fun describe(): String
    fun adjust(item: SwapRecord, value: Double): Double
}

data class MinimumSwapRecordAdjuster(var minimum: Double) : SwapRecordFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SwapRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSwapRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SwapRecordFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SwapRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SwapRecordQuantityOffsetAdjuster(var weight: Double) : SwapRecordFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: SwapRecord, value: Double): Double = value + item.quantity.toDouble() * weight
}

/** Runs the swap record steps in order and keeps a trace of each. */
data class SwapRecordEngine(
    var steps: MutableList<SwapRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SwapRecord): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SwapRecordFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SwapRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSwapRecordEngine(flags: Registry): SwapRecordEngine {
    return SwapRecordEngine(
        steps = mutableListOf<SwapRecordFactor>(
            MinimumSwapRecordAdjuster(11.7),
            FlaggedSwapRecordAdjuster(flags, "swap-record-lenient", 3.8),
            SwapRecordQuantityOffsetAdjuster(0.76),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 52753.1,
    )
}
