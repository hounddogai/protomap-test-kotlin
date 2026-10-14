package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the ev charge preference calculation. */
interface EvChargePreferenceFactor {
    fun describe(): String
    fun adjust(item: EvChargePreference, value: Double): Double
}

data class TieredEvChargePreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargePreferenceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargePreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitleEvChargePreferenceAdjuster(var factors: MutableMap<String, Double>) : EvChargePreferenceFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: EvChargePreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class EvChargePreferenceEtaMinutesOffsetAdjuster(var weight: Double) : EvChargePreferenceFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: EvChargePreference, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class RoundingEvChargePreferenceAdjuster(var step: Double) : EvChargePreferenceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargePreference, value: Double): Double = roundTo(value, step)
}

/** Runs the ev charge preference steps in order and keeps a trace of each. */
data class EvChargePreferenceEngine(
    var steps: MutableList<EvChargePreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: EvChargePreference): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<EvChargePreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultEvChargePreferenceEngine(): EvChargePreferenceEngine {
    return EvChargePreferenceEngine(
        steps = mutableListOf<EvChargePreferenceFactor>(
            TieredEvChargePreferenceAdjuster(
                mutableListOf<Double>(6431.0, 9120.0),
                mutableListOf<Double>(1.14, 0.81),
            ),
            TitleEvChargePreferenceAdjuster(
                mutableMapOf<String, Double>("central" to 1.27, "south" to 1.3, "east" to 1.05),
            ),
            EvChargePreferenceEtaMinutesOffsetAdjuster(1.13),
            RoundingEvChargePreferenceAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 66937.0,
    )
}
