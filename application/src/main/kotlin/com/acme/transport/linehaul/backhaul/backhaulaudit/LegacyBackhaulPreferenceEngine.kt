package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy backhaul preference calculation. */
interface LegacyBackhaulPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: LegacyBackhaulPreference, value: Double): Double
}

data class MinimumLegacyBackhaulPreferenceAdjuster(var minimum: Double) : LegacyBackhaulPreferenceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyBackhaulPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredLegacyBackhaulPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyBackhaulPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyBackhaulPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLegacyBackhaulPreferenceAdjuster(var cap: Double) : LegacyBackhaulPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyBackhaulPreference, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy backhaul preference steps in order and keeps a trace of each. */
data class LegacyBackhaulPreferenceEngine(
    var steps: MutableList<LegacyBackhaulPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyBackhaulPreference): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyBackhaulPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyBackhaulPreferenceEngine(): LegacyBackhaulPreferenceEngine {
    return LegacyBackhaulPreferenceEngine(
        steps = mutableListOf<LegacyBackhaulPreferenceAdjuster>(
            MinimumLegacyBackhaulPreferenceAdjuster(32.6),
            TieredLegacyBackhaulPreferenceAdjuster(
                mutableListOf<Double>(2521.0, 5666.0, 6619.0),
                mutableListOf<Double>(0.99, 0.92, 1.01),
            ),
            CapLegacyBackhaulPreferenceAdjuster(22240.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 72912.9,
    )
}
