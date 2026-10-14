package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy cross dock exception calculation. */
interface LegacyCrossDockExceptionModifier {
    fun describe(): String
    fun adjust(item: LegacyCrossDockException, value: Double): Double
}

data class PercentLegacyCrossDockExceptionAdjuster(var percent: Double) : LegacyCrossDockExceptionModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyCrossDockException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLegacyCrossDockExceptionAdjuster(var minimum: Double) : LegacyCrossDockExceptionModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyCrossDockException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocaleLegacyCrossDockExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyCrossDockExceptionModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: LegacyCrossDockException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the legacy cross dock exception steps in order and keeps a trace of each. */
data class LegacyCrossDockExceptionEngine(
    var steps: MutableList<LegacyCrossDockExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyCrossDockException): Double {
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

    fun evaluateAll(items: MutableList<LegacyCrossDockException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyCrossDockExceptionEngine(): LegacyCrossDockExceptionEngine {
    return LegacyCrossDockExceptionEngine(
        steps = mutableListOf<LegacyCrossDockExceptionModifier>(
            PercentLegacyCrossDockExceptionAdjuster(22.9),
            MinimumLegacyCrossDockExceptionAdjuster(44.9),
            LocaleLegacyCrossDockExceptionAdjuster(mutableMapOf<String, Double>("west" to 1.32, "east" to 0.84)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 64817.0,
    )
}
