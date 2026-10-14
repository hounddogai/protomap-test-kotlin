package com.acme.transport.network.servicearea.agreement

/** One step of the lenient service area rollup calculation. */
interface LenientServiceAreaRollupAdjuster {
    fun describe(): String
    fun adjust(item: LenientServiceAreaRollup, value: Double): Double
}

data class TitleLenientServiceAreaRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientServiceAreaRollupAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientServiceAreaRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumLenientServiceAreaRollupAdjuster(var minimum: Double) : LenientServiceAreaRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientServiceAreaRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapLenientServiceAreaRollupAdjuster(var cap: Double) : LenientServiceAreaRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientServiceAreaRollup, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient service area rollup steps in order and keeps a trace of each. */
data class LenientServiceAreaRollupEngine(
    var steps: MutableList<LenientServiceAreaRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientServiceAreaRollup): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientServiceAreaRollupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientServiceAreaRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientServiceAreaRollupEngine(): LenientServiceAreaRollupEngine {
    return LenientServiceAreaRollupEngine(
        steps = mutableListOf<LenientServiceAreaRollupAdjuster>(
            TitleLenientServiceAreaRollupAdjuster(mutableMapOf<String, Double>("east" to 1.38)),
            MinimumLenientServiceAreaRollupAdjuster(31.3),
            CapLenientServiceAreaRollupAdjuster(19666.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 48229.2,
    )
}
