package com.acme.transport.terminal.crossdock.regionaldockclaim

import com.acme.transport.kit.flags.Registry

/** One step of the global cross dock preference calculation. */
interface GlobalCrossDockPreferenceModifier {
    fun describe(): String
    fun adjust(item: GlobalCrossDockPreference, value: Double): Double
}

data class MinimumGlobalCrossDockPreferenceAdjuster(var minimum: Double) : GlobalCrossDockPreferenceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalCrossDockPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentGlobalCrossDockPreferenceAdjuster(var percent: Double) : GlobalCrossDockPreferenceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalCrossDockPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class GlobalCrossDockPreferenceMinimumOffsetAdjuster(
    var weight: Double,
) : GlobalCrossDockPreferenceModifier {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: GlobalCrossDockPreference, value: Double): Double = value + item.minimum * weight
}

data class TieredGlobalCrossDockPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalCrossDockPreferenceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalCrossDockPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedGlobalCrossDockPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalCrossDockPreferenceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalCrossDockPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapGlobalCrossDockPreferenceAdjuster(var cap: Double) : GlobalCrossDockPreferenceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalCrossDockPreference, value: Double): Double = minOf(value, cap)
}

/** Runs the global cross dock preference steps in order and keeps a trace of each. */
data class GlobalCrossDockPreferenceEngine(
    var steps: MutableList<GlobalCrossDockPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalCrossDockPreference): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalCrossDockPreferenceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalCrossDockPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalCrossDockPreferenceEngine(flags: Registry): GlobalCrossDockPreferenceEngine {
    return GlobalCrossDockPreferenceEngine(
        steps = mutableListOf<GlobalCrossDockPreferenceModifier>(
            MinimumGlobalCrossDockPreferenceAdjuster(42.2),
            PercentGlobalCrossDockPreferenceAdjuster(-10.1),
            GlobalCrossDockPreferenceMinimumOffsetAdjuster(0.49),
            TieredGlobalCrossDockPreferenceAdjuster(
                mutableListOf<Double>(2183.0, 4907.0, 5755.0, 6025.0),
                mutableListOf<Double>(0.84, 1.08, 0.93, 1.01),
            ),
            FlaggedGlobalCrossDockPreferenceAdjuster(flags, "global-cross-dock-preference-legacy", -5.4),
            CapGlobalCrossDockPreferenceAdjuster(47385.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 43468.1,
    )
}
