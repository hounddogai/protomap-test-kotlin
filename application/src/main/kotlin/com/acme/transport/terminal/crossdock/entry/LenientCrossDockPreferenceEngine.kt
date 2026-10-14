package com.acme.transport.terminal.crossdock.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient cross dock preference calculation. */
interface LenientCrossDockPreferenceFactor {
    fun describe(): String
    fun adjust(item: LenientCrossDockPreference, value: Double): Double
}

data class MinimumLenientCrossDockPreferenceAdjuster(var minimum: Double) : LenientCrossDockPreferenceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientCrossDockPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLenientCrossDockPreferenceAdjuster(var step: Double) : LenientCrossDockPreferenceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientCrossDockPreference, value: Double): Double = roundTo(value, step)
}

data class FlaggedLenientCrossDockPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientCrossDockPreferenceFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientCrossDockPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLenientCrossDockPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientCrossDockPreferenceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientCrossDockPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLenientCrossDockPreferenceAdjuster(var percent: Double) : LenientCrossDockPreferenceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientCrossDockPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the lenient cross dock preference steps in order and keeps a trace of each. */
data class LenientCrossDockPreferenceEngine(
    var steps: MutableList<LenientCrossDockPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientCrossDockPreference): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientCrossDockPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientCrossDockPreferenceEngine(flags: Registry): LenientCrossDockPreferenceEngine {
    return LenientCrossDockPreferenceEngine(
        steps = mutableListOf<LenientCrossDockPreferenceFactor>(
            MinimumLenientCrossDockPreferenceAdjuster(15.3),
            RoundingLenientCrossDockPreferenceAdjuster(0.5),
            FlaggedLenientCrossDockPreferenceAdjuster(flags, "lenient-cross-dock-preference-pending", -8.6),
            TieredLenientCrossDockPreferenceAdjuster(
                mutableListOf<Double>(1034.0, 5038.0, 7740.0),
                mutableListOf<Double>(0.83, 1.05, 0.83),
            ),
            PercentLenientCrossDockPreferenceAdjuster(-8.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 84932.8,
    )
}
