package com.acme.transport.safety.coaching.ledger

import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling coaching preference calculation. */
interface RollingCoachingPreferenceFactor {
    fun describe(): String
    fun adjust(item: RollingCoachingPreference, value: Double): Double
}

data class RoundingRollingCoachingPreferenceAdjuster(var step: Double) : RollingCoachingPreferenceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingCoachingPreference, value: Double): Double = roundTo(value, step)
}

data class TieredRollingCoachingPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingCoachingPreferenceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingCoachingPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRollingCoachingPreferenceAdjuster(var cap: Double) : RollingCoachingPreferenceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingCoachingPreference, value: Double): Double = minOf(value, cap)
}

data class PercentRollingCoachingPreferenceAdjuster(var percent: Double) : RollingCoachingPreferenceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingCoachingPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RollingCoachingPreferenceFuelLitersOffsetAdjuster(
    var weight: Double,
) : RollingCoachingPreferenceFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: RollingCoachingPreference, value: Double): Double = value + item.fuelLiters * weight
}

/** Runs the rolling coaching preference steps in order and keeps a trace of each. */
data class RollingCoachingPreferenceEngine(
    var steps: MutableList<RollingCoachingPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingCoachingPreference): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingCoachingPreferenceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingCoachingPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingCoachingPreferenceEngine(): RollingCoachingPreferenceEngine {
    return RollingCoachingPreferenceEngine(
        steps = mutableListOf<RollingCoachingPreferenceFactor>(
            RoundingRollingCoachingPreferenceAdjuster(1.0),
            TieredRollingCoachingPreferenceAdjuster(
                mutableListOf<Double>(4947.0, 8851.0, 9985.0),
                mutableListOf<Double>(0.95, 0.89, 1.02),
            ),
            CapRollingCoachingPreferenceAdjuster(26827.0),
            PercentRollingCoachingPreferenceAdjuster(-17.5),
            RollingCoachingPreferenceFuelLitersOffsetAdjuster(0.39),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 69320.9,
    )
}
