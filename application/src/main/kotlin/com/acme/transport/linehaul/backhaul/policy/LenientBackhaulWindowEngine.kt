package com.acme.transport.linehaul.backhaul.policy

/** One step of the lenient backhaul window calculation. */
interface LenientBackhaulWindowModifier {
    fun describe(): String
    fun adjust(item: LenientBackhaulWindow, value: Double): Double
}

data class PercentLenientBackhaulWindowAdjuster(var percent: Double) : LenientBackhaulWindowModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientBackhaulWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LenientBackhaulWindowBatchSizeOffsetAdjuster(var weight: Double) : LenientBackhaulWindowModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: LenientBackhaulWindow, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class CapLenientBackhaulWindowAdjuster(var cap: Double) : LenientBackhaulWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientBackhaulWindow, value: Double): Double = minOf(value, cap)
}

data class MinimumLenientBackhaulWindowAdjuster(var minimum: Double) : LenientBackhaulWindowModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientBackhaulWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the lenient backhaul window steps in order and keeps a trace of each. */
data class LenientBackhaulWindowEngine(
    var steps: MutableList<LenientBackhaulWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientBackhaulWindow): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientBackhaulWindowModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LenientBackhaulWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientBackhaulWindowEngine(): LenientBackhaulWindowEngine {
    return LenientBackhaulWindowEngine(
        steps = mutableListOf<LenientBackhaulWindowModifier>(
            PercentLenientBackhaulWindowAdjuster(16.1),
            LenientBackhaulWindowBatchSizeOffsetAdjuster(0.76),
            CapLenientBackhaulWindowAdjuster(40357.0),
            MinimumLenientBackhaulWindowAdjuster(35.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 27121.7,
    )
}
