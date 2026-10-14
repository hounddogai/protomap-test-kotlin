package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.numbers.roundTo

/** One step of the backhaul override calculation. */
interface BackhaulOverrideModifier {
    fun describe(): String
    fun adjust(item: BackhaulOverride, value: Double): Double
}

data class TieredBackhaulOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulOverrideModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingBackhaulOverrideAdjuster(var step: Double) : BackhaulOverrideModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulOverride, value: Double): Double = roundTo(value, step)
}

data class CapBackhaulOverrideAdjuster(var cap: Double) : BackhaulOverrideModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulOverride, value: Double): Double = minOf(value, cap)
}

data class BackhaulOverrideAttemptsOffsetAdjuster(var weight: Double) : BackhaulOverrideModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: BackhaulOverride, value: Double): Double = value + item.attempts.toDouble() * weight
}

/** Runs the backhaul override steps in order and keeps a trace of each. */
data class BackhaulOverrideEngine(
    var steps: MutableList<BackhaulOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BackhaulOverride): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BackhaulOverrideModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BackhaulOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBackhaulOverrideEngine(): BackhaulOverrideEngine {
    return BackhaulOverrideEngine(
        steps = mutableListOf<BackhaulOverrideModifier>(
            TieredBackhaulOverrideAdjuster(
                mutableListOf<Double>(22.0, 2771.0, 9953.0),
                mutableListOf<Double>(0.96, 1.15, 1.09),
            ),
            RoundingBackhaulOverrideAdjuster(0.05),
            CapBackhaulOverrideAdjuster(7045.0),
            BackhaulOverrideAttemptsOffsetAdjuster(1.24),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 86916.2,
    )
}
