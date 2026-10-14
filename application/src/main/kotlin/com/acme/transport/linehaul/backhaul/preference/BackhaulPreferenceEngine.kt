package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the backhaul preference calculation. */
interface BackhaulPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: BackhaulPreference, value: Double): Double
}

data class TieredBackhaulPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapBackhaulPreferenceAdjuster(var cap: Double) : BackhaulPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulPreference, value: Double): Double = minOf(value, cap)
}

data class BackhaulPreferenceFuelLitersOffsetAdjuster(var weight: Double) : BackhaulPreferenceAdjuster {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: BackhaulPreference, value: Double): Double = value + item.fuelLiters * weight
}

data class RoundingBackhaulPreferenceAdjuster(var step: Double) : BackhaulPreferenceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulPreference, value: Double): Double = roundTo(value, step)
}

/** Runs the backhaul preference steps in order and keeps a trace of each. */
data class BackhaulPreferenceEngine(
    var steps: MutableList<BackhaulPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BackhaulPreference): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBackhaulPreferenceEngine(): BackhaulPreferenceEngine {
    return BackhaulPreferenceEngine(
        steps = mutableListOf<BackhaulPreferenceAdjuster>(
            TieredBackhaulPreferenceAdjuster(
                mutableListOf<Double>(4174.0, 6167.0),
                mutableListOf<Double>(1.13, 1.15),
            ),
            CapBackhaulPreferenceAdjuster(31833.0),
            BackhaulPreferenceFuelLitersOffsetAdjuster(0.69),
            RoundingBackhaulPreferenceAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 28484.2,
    )
}
