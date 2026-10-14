package com.acme.transport.safety.incident.digest

/** One step of the tiered incident preference calculation. */
interface TieredIncidentPreferenceFactor {
    fun describe(): String
    fun adjust(item: TieredIncidentPreference, value: Double): Double
}

data class TieredTieredIncidentPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredIncidentPreferenceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredIncidentPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTieredIncidentPreferenceAdjuster(var percent: Double) : TieredIncidentPreferenceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredIncidentPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredIncidentPreferenceScoreOffsetAdjuster(var weight: Double) : TieredIncidentPreferenceFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: TieredIncidentPreference, value: Double): Double = value + item.score * weight
}

data class MinimumTieredIncidentPreferenceAdjuster(var minimum: Double) : TieredIncidentPreferenceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredIncidentPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tiered incident preference steps in order and keeps a trace of each. */
data class TieredIncidentPreferenceEngine(
    var steps: MutableList<TieredIncidentPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TieredIncidentPreference): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredIncidentPreferenceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TieredIncidentPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTieredIncidentPreferenceEngine(): TieredIncidentPreferenceEngine {
    return TieredIncidentPreferenceEngine(
        steps = mutableListOf<TieredIncidentPreferenceFactor>(
            TieredTieredIncidentPreferenceAdjuster(
                mutableListOf<Double>(4320.0, 6240.0),
                mutableListOf<Double>(1.01, 1.16),
            ),
            PercentTieredIncidentPreferenceAdjuster(18.0),
            TieredIncidentPreferenceScoreOffsetAdjuster(0.04),
            MinimumTieredIncidentPreferenceAdjuster(10.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 36424.2,
    )
}
