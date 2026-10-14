package com.acme.transport.emissions.evcharge.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the global ev charge override calculation. */
interface GlobalEvChargeOverrideFactor {
    fun describe(): String
    fun adjust(item: GlobalEvChargeOverride, value: Double): Double
}

data class GlobalEvChargeOverrideEtaMinutesOffsetAdjuster(var weight: Double) : GlobalEvChargeOverrideFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: GlobalEvChargeOverride, value: Double): Double {
        return value + item.etaMinutes.toDouble() * weight
    }
}

data class LocaleGlobalEvChargeOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalEvChargeOverrideFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: GlobalEvChargeOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedGlobalEvChargeOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalEvChargeOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalEvChargeOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalEvChargeOverrideAdjuster(var step: Double) : GlobalEvChargeOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalEvChargeOverride, value: Double): Double = roundTo(value, step)
}

data class TieredGlobalEvChargeOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalEvChargeOverrideFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalEvChargeOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalEvChargeOverrideAdjuster(var cap: Double) : GlobalEvChargeOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalEvChargeOverride, value: Double): Double = minOf(value, cap)
}

/** Runs the global ev charge override steps in order and keeps a trace of each. */
data class GlobalEvChargeOverrideEngine(
    var steps: MutableList<GlobalEvChargeOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalEvChargeOverride): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalEvChargeOverrideFactor = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalEvChargeOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalEvChargeOverrideEngine(flags: Registry): GlobalEvChargeOverrideEngine {
    return GlobalEvChargeOverrideEngine(
        steps = mutableListOf<GlobalEvChargeOverrideFactor>(
            GlobalEvChargeOverrideEtaMinutesOffsetAdjuster(1.57),
            LocaleGlobalEvChargeOverrideAdjuster(mutableMapOf<String, Double>("east" to 1.31)),
            FlaggedGlobalEvChargeOverrideAdjuster(flags, "global-ev-charge-override-pending", -2.8),
            RoundingGlobalEvChargeOverrideAdjuster(0.25),
            TieredGlobalEvChargeOverrideAdjuster(
                mutableListOf<Double>(289.0, 9482.0),
                mutableListOf<Double>(1.01, 1.02),
            ),
            CapGlobalEvChargeOverrideAdjuster(46123.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 95284.8,
    )
}
