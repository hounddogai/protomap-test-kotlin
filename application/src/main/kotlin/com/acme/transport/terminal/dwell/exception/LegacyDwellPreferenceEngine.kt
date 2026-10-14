package com.acme.transport.terminal.dwell.exception

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy dwell preference calculation. */
interface LegacyDwellPreferenceModifier {
    fun describe(): String
    fun adjust(item: LegacyDwellPreference, value: Double): Double
}

data class UnitLegacyDwellPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyDwellPreferenceModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: LegacyDwellPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLegacyDwellPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyDwellPreferenceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyDwellPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLegacyDwellPreferenceAdjuster(var percent: Double) : LegacyDwellPreferenceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyDwellPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLegacyDwellPreferenceAdjuster(var step: Double) : LegacyDwellPreferenceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyDwellPreference, value: Double): Double = roundTo(value, step)
}

data class MinimumLegacyDwellPreferenceAdjuster(var minimum: Double) : LegacyDwellPreferenceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyDwellPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy dwell preference steps in order and keeps a trace of each. */
data class LegacyDwellPreferenceEngine(
    var steps: MutableList<LegacyDwellPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyDwellPreference): Double {
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

    fun evaluateAll(items: MutableList<LegacyDwellPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyDwellPreferenceEngine(): LegacyDwellPreferenceEngine {
    return LegacyDwellPreferenceEngine(
        steps = mutableListOf<LegacyDwellPreferenceModifier>(
            UnitLegacyDwellPreferenceAdjuster(mutableMapOf<String, Double>("retail" to 1.08)),
            TieredLegacyDwellPreferenceAdjuster(
                mutableListOf<Double>(193.0, 3686.0, 3976.0, 9683.0),
                mutableListOf<Double>(1.09, 0.9, 1.14, 0.95),
            ),
            PercentLegacyDwellPreferenceAdjuster(4.6),
            RoundingLegacyDwellPreferenceAdjuster(0.25),
            MinimumLegacyDwellPreferenceAdjuster(47.4),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 70710.4,
    )
}
