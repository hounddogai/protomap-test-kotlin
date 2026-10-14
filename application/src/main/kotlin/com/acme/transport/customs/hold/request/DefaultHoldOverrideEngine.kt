package com.acme.transport.customs.hold.request

import com.acme.transport.kit.numbers.roundTo

/** One step of the default hold override calculation. */
interface DefaultHoldOverrideFactor {
    fun describe(): String
    fun adjust(item: DefaultHoldOverride, value: Double): Double
}

data class PercentDefaultHoldOverrideAdjuster(var percent: Double) : DefaultHoldOverrideFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultHoldOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionDefaultHoldOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultHoldOverrideFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: DefaultHoldOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDefaultHoldOverrideAdjuster(var cap: Double) : DefaultHoldOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultHoldOverride, value: Double): Double = minOf(value, cap)
}

data class MinimumDefaultHoldOverrideAdjuster(var minimum: Double) : DefaultHoldOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultHoldOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingDefaultHoldOverrideAdjuster(var step: Double) : DefaultHoldOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultHoldOverride, value: Double): Double = roundTo(value, step)
}

data class DefaultHoldOverrideQuantityOffsetAdjuster(var weight: Double) : DefaultHoldOverrideFactor {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: DefaultHoldOverride, value: Double): Double = value + item.quantity.toDouble() * weight
}

/** Runs the default hold override steps in order and keeps a trace of each. */
data class DefaultHoldOverrideEngine(
    var steps: MutableList<DefaultHoldOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultHoldOverride): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultHoldOverrideFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultHoldOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultHoldOverrideEngine(): DefaultHoldOverrideEngine {
    return DefaultHoldOverrideEngine(
        steps = mutableListOf<DefaultHoldOverrideFactor>(
            PercentDefaultHoldOverrideAdjuster(-6.1),
            RegionDefaultHoldOverrideAdjuster(mutableMapOf<String, Double>("east" to 0.75, "west" to 1.27)),
            CapDefaultHoldOverrideAdjuster(23619.0),
            MinimumDefaultHoldOverrideAdjuster(49.7),
            RoundingDefaultHoldOverrideAdjuster(0.25),
            DefaultHoldOverrideQuantityOffsetAdjuster(0.12),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 29098.5,
    )
}
