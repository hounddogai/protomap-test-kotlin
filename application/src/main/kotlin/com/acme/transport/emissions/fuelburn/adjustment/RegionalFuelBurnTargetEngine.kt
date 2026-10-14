package com.acme.transport.emissions.fuelburn.adjustment

import com.acme.transport.kit.flags.Registry

/** One step of the regional fuel burn target calculation. */
interface RegionalFuelBurnTargetModifier {
    fun describe(): String
    fun adjust(item: RegionalFuelBurnTarget, value: Double): Double
}

data class TieredRegionalFuelBurnTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalFuelBurnTargetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalFuelBurnTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRegionalFuelBurnTargetAdjuster(var percent: Double) : RegionalFuelBurnTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalFuelBurnTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionalFuelBurnTargetVersionOffsetAdjuster(var weight: Double) : RegionalFuelBurnTargetModifier {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: RegionalFuelBurnTarget, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class FlaggedRegionalFuelBurnTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalFuelBurnTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalFuelBurnTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalFuelBurnTargetAdjuster(var cap: Double) : RegionalFuelBurnTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalFuelBurnTarget, value: Double): Double = minOf(value, cap)
}

data class MinimumRegionalFuelBurnTargetAdjuster(var minimum: Double) : RegionalFuelBurnTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalFuelBurnTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional fuel burn target steps in order and keeps a trace of each. */
data class RegionalFuelBurnTargetEngine(
    var steps: MutableList<RegionalFuelBurnTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalFuelBurnTarget): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalFuelBurnTargetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalFuelBurnTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalFuelBurnTargetEngine(flags: Registry): RegionalFuelBurnTargetEngine {
    return RegionalFuelBurnTargetEngine(
        steps = mutableListOf<RegionalFuelBurnTargetModifier>(
            TieredRegionalFuelBurnTargetAdjuster(
                mutableListOf<Double>(344.0, 5812.0, 9684.0),
                mutableListOf<Double>(1.08, 1.18, 0.92),
            ),
            PercentRegionalFuelBurnTargetAdjuster(-11.6),
            RegionalFuelBurnTargetVersionOffsetAdjuster(0.83),
            FlaggedRegionalFuelBurnTargetAdjuster(flags, "regional-fuel-burn-target-scheduled", 0.5),
            CapRegionalFuelBurnTargetAdjuster(10629.0),
            MinimumRegionalFuelBurnTargetAdjuster(36.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 93183.6,
    )
}
