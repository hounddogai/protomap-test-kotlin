package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict incident preference calculation. */
interface StrictIncidentPreferenceModifier {
    fun describe(): String
    fun adjust(item: StrictIncidentPreference, value: Double): Double
}

data class StrictIncidentPreferenceCapacityOffsetAdjuster(
    var weight: Double,
) : StrictIncidentPreferenceModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: StrictIncidentPreference, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class MinimumStrictIncidentPreferenceAdjuster(var minimum: Double) : StrictIncidentPreferenceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictIncidentPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedStrictIncidentPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictIncidentPreferenceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictIncidentPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredStrictIncidentPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictIncidentPreferenceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictIncidentPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict incident preference steps in order and keeps a trace of each. */
data class StrictIncidentPreferenceEngine(
    var steps: MutableList<StrictIncidentPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictIncidentPreference): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<StrictIncidentPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictIncidentPreferenceEngine(flags: Registry): StrictIncidentPreferenceEngine {
    return StrictIncidentPreferenceEngine(
        steps = mutableListOf<StrictIncidentPreferenceModifier>(
            StrictIncidentPreferenceCapacityOffsetAdjuster(0.48),
            MinimumStrictIncidentPreferenceAdjuster(27.7),
            FlaggedStrictIncidentPreferenceAdjuster(flags, "strict-incident-preference-seasonal", -9.0),
            TieredStrictIncidentPreferenceAdjuster(
                mutableListOf<Double>(155.0, 1491.0, 4471.0),
                mutableListOf<Double>(1.16, 0.96, 1.06),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 69990.4,
    )
}
