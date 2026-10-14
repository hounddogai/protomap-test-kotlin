package com.acme.transport.linehaul.backhaul.mapping

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default backhaul window calculation. */
interface DefaultBackhaulWindowAdjuster {
    fun describe(): String
    fun adjust(item: DefaultBackhaulWindow, value: Double): Double
}

data class LabelDefaultBackhaulWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultBackhaulWindowAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: DefaultBackhaulWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredDefaultBackhaulWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultBackhaulWindowAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultBackhaulWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DefaultBackhaulWindowCostOffsetAdjuster(var weight: Double) : DefaultBackhaulWindowAdjuster {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: DefaultBackhaulWindow, value: Double): Double = value + item.cost * weight
}

data class PercentDefaultBackhaulWindowAdjuster(var percent: Double) : DefaultBackhaulWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultBackhaulWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDefaultBackhaulWindowAdjuster(var cap: Double) : DefaultBackhaulWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultBackhaulWindow, value: Double): Double = minOf(value, cap)
}

data class MinimumDefaultBackhaulWindowAdjuster(var minimum: Double) : DefaultBackhaulWindowAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultBackhaulWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default backhaul window steps in order and keeps a trace of each. */
data class DefaultBackhaulWindowEngine(
    var steps: MutableList<DefaultBackhaulWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultBackhaulWindow): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultBackhaulWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultBackhaulWindowEngine(): DefaultBackhaulWindowEngine {
    return DefaultBackhaulWindowEngine(
        steps = mutableListOf<DefaultBackhaulWindowAdjuster>(
            LabelDefaultBackhaulWindowAdjuster(mutableMapOf<String, Double>("west" to 1.06, "north" to 1.19)),
            TieredDefaultBackhaulWindowAdjuster(
                mutableListOf<Double>(3893.0, 4186.0, 9279.0),
                mutableListOf<Double>(1.1, 1.11, 1.07),
            ),
            DefaultBackhaulWindowCostOffsetAdjuster(1.29),
            PercentDefaultBackhaulWindowAdjuster(-13.8),
            CapDefaultBackhaulWindowAdjuster(40028.0),
            MinimumDefaultBackhaulWindowAdjuster(41.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 7887.4,
    )
}
