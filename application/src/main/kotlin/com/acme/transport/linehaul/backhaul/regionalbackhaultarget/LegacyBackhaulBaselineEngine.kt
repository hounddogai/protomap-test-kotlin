package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy backhaul baseline calculation. */
interface LegacyBackhaulBaselineAdjuster {
    fun describe(): String
    fun adjust(item: LegacyBackhaulBaseline, value: Double): Double
}

data class TieredLegacyBackhaulBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyBackhaulBaselineAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyBackhaulBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLegacyBackhaulBaselineAdjuster(var percent: Double) : LegacyBackhaulBaselineAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyBackhaulBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLegacyBackhaulBaselineAdjuster(var minimum: Double) : LegacyBackhaulBaselineAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyBackhaulBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLegacyBackhaulBaselineAdjuster(var step: Double) : LegacyBackhaulBaselineAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyBackhaulBaseline, value: Double): Double = roundTo(value, step)
}

/** Runs the legacy backhaul baseline steps in order and keeps a trace of each. */
data class LegacyBackhaulBaselineEngine(
    var steps: MutableList<LegacyBackhaulBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyBackhaulBaseline): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyBackhaulBaselineAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyBackhaulBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyBackhaulBaselineEngine(): LegacyBackhaulBaselineEngine {
    return LegacyBackhaulBaselineEngine(
        steps = mutableListOf<LegacyBackhaulBaselineAdjuster>(
            TieredLegacyBackhaulBaselineAdjuster(
                mutableListOf<Double>(6124.0, 8091.0),
                mutableListOf<Double>(1.14, 1.01),
            ),
            PercentLegacyBackhaulBaselineAdjuster(10.8),
            MinimumLegacyBackhaulBaselineAdjuster(28.8),
            RoundingLegacyBackhaulBaselineAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 10658.8,
    )
}
