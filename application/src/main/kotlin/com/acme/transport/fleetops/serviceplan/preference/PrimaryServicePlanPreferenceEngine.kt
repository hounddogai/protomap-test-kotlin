package com.acme.transport.fleetops.serviceplan.preference

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary service plan preference calculation. */
interface PrimaryServicePlanPreferenceModifier {
    fun describe(): String
    fun adjust(item: PrimaryServicePlanPreference, value: Double): Double
}

data class MinimumPrimaryServicePlanPreferenceAdjuster(
    var minimum: Double,
) : PrimaryServicePlanPreferenceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryServicePlanPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PrimaryServicePlanPreferenceAttemptsOffsetAdjuster(
    var weight: Double,
) : PrimaryServicePlanPreferenceModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: PrimaryServicePlanPreference, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class CapPrimaryServicePlanPreferenceAdjuster(var cap: Double) : PrimaryServicePlanPreferenceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryServicePlanPreference, value: Double): Double = minOf(value, cap)
}

data class TieredPrimaryServicePlanPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryServicePlanPreferenceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryServicePlanPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary service plan preference steps in order and keeps a trace of each. */
data class PrimaryServicePlanPreferenceEngine(
    var steps: MutableList<PrimaryServicePlanPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryServicePlanPreference): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryServicePlanPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryServicePlanPreferenceEngine(): PrimaryServicePlanPreferenceEngine {
    return PrimaryServicePlanPreferenceEngine(
        steps = mutableListOf<PrimaryServicePlanPreferenceModifier>(
            MinimumPrimaryServicePlanPreferenceAdjuster(41.6),
            PrimaryServicePlanPreferenceAttemptsOffsetAdjuster(1.39),
            CapPrimaryServicePlanPreferenceAdjuster(47521.0),
            TieredPrimaryServicePlanPreferenceAdjuster(
                mutableListOf<Double>(2852.0, 3363.0, 4108.0),
                mutableListOf<Double>(1.01, 0.84, 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 77415.8,
    )
}
