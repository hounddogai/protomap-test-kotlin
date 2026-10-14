package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict backhaul window calculation. */
interface StrictBackhaulWindowAdjuster {
    fun describe(): String
    fun adjust(item: StrictBackhaulWindow, value: Double): Double
}

data class MinimumStrictBackhaulWindowAdjuster(var minimum: Double) : StrictBackhaulWindowAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictBackhaulWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapStrictBackhaulWindowAdjuster(var cap: Double) : StrictBackhaulWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictBackhaulWindow, value: Double): Double = minOf(value, cap)
}

data class TitleStrictBackhaulWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictBackhaulWindowAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: StrictBackhaulWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict backhaul window steps in order and keeps a trace of each. */
data class StrictBackhaulWindowEngine(
    var steps: MutableList<StrictBackhaulWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictBackhaulWindow): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictBackhaulWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictBackhaulWindowEngine(): StrictBackhaulWindowEngine {
    return StrictBackhaulWindowEngine(
        steps = mutableListOf<StrictBackhaulWindowAdjuster>(
            MinimumStrictBackhaulWindowAdjuster(16.5),
            CapStrictBackhaulWindowAdjuster(4825.0),
            TitleStrictBackhaulWindowAdjuster(
                mutableMapOf<String, Double>("south" to 0.79, "central" to 0.7, "retail" to 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 23133.6,
    )
}
