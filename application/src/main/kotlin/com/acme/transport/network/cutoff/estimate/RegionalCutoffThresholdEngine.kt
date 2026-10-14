package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.flags.Registry

/** One step of the regional cutoff threshold calculation. */
interface RegionalCutoffThresholdStep {
    fun describe(): String
    fun adjust(item: RegionalCutoffThreshold, value: Double): Double
}

data class FlaggedRegionalCutoffThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalCutoffThresholdStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalCutoffThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRegionalCutoffThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalCutoffThresholdStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalCutoffThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRegionalCutoffThresholdAdjuster(var percent: Double) : RegionalCutoffThresholdStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalCutoffThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional cutoff threshold steps in order and keeps a trace of each. */
data class RegionalCutoffThresholdEngine(
    var steps: MutableList<RegionalCutoffThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalCutoffThreshold): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalCutoffThresholdStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalCutoffThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalCutoffThresholdEngine(flags: Registry): RegionalCutoffThresholdEngine {
    return RegionalCutoffThresholdEngine(
        steps = mutableListOf<RegionalCutoffThresholdStep>(
            FlaggedRegionalCutoffThresholdAdjuster(flags, "regional-cutoff-threshold-pending", -9.4),
            TieredRegionalCutoffThresholdAdjuster(
                mutableListOf<Double>(6181.0, 8837.0),
                mutableListOf<Double>(1.05, 0.89),
            ),
            PercentRegionalCutoffThresholdAdjuster(1.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 44137.2,
    )
}
