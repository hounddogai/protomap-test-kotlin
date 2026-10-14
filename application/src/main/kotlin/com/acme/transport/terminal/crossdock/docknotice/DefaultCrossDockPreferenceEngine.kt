package com.acme.transport.terminal.crossdock.docknotice

import com.acme.transport.kit.numbers.roundTo

/** One step of the default cross dock preference calculation. */
interface DefaultCrossDockPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: DefaultCrossDockPreference, value: Double): Double
}

data class DefaultCrossDockPreferenceCreatedAtOffsetAdjuster(
    var weight: Double,
) : DefaultCrossDockPreferenceAdjuster {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: DefaultCrossDockPreference, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class RoundingDefaultCrossDockPreferenceAdjuster(var step: Double) : DefaultCrossDockPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultCrossDockPreference, value: Double): Double = roundTo(value, step)
}

data class MinimumDefaultCrossDockPreferenceAdjuster(var minimum: Double) : DefaultCrossDockPreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultCrossDockPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredDefaultCrossDockPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultCrossDockPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultCrossDockPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the default cross dock preference steps in order and keeps a trace of each. */
data class DefaultCrossDockPreferenceEngine(
    var steps: MutableList<DefaultCrossDockPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultCrossDockPreference): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultCrossDockPreferenceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultCrossDockPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultCrossDockPreferenceEngine(): DefaultCrossDockPreferenceEngine {
    return DefaultCrossDockPreferenceEngine(
        steps = mutableListOf<DefaultCrossDockPreferenceAdjuster>(
            DefaultCrossDockPreferenceCreatedAtOffsetAdjuster(0.71),
            RoundingDefaultCrossDockPreferenceAdjuster(0.05),
            MinimumDefaultCrossDockPreferenceAdjuster(14.8),
            TieredDefaultCrossDockPreferenceAdjuster(
                mutableListOf<Double>(196.0, 2506.0, 4392.0, 7462.0),
                mutableListOf<Double>(0.87, 1.14, 1.12, 1.16),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 30694.0,
    )
}
