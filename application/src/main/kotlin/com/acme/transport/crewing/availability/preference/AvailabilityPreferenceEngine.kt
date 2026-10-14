package com.acme.transport.crewing.availability.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the availability preference calculation. */
interface AvailabilityPreferenceModifier {
    fun describe(): String
    fun adjust(item: AvailabilityPreference, value: Double): Double
}

data class AvailabilityPreferenceLimitOffsetAdjuster(var weight: Double) : AvailabilityPreferenceModifier {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: AvailabilityPreference, value: Double): Double = value + item.limit.toDouble() * weight
}

data class FlaggedAvailabilityPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilityPreferenceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilityPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleAvailabilityPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : AvailabilityPreferenceModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: AvailabilityPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredAvailabilityPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AvailabilityPreferenceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AvailabilityPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentAvailabilityPreferenceAdjuster(var percent: Double) : AvailabilityPreferenceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AvailabilityPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the availability preference steps in order and keeps a trace of each. */
data class AvailabilityPreferenceEngine(
    var steps: MutableList<AvailabilityPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AvailabilityPreference): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AvailabilityPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAvailabilityPreferenceEngine(flags: Registry): AvailabilityPreferenceEngine {
    return AvailabilityPreferenceEngine(
        steps = mutableListOf<AvailabilityPreferenceModifier>(
            AvailabilityPreferenceLimitOffsetAdjuster(0.49),
            FlaggedAvailabilityPreferenceAdjuster(flags, "availability-preference-scheduled", 5.8),
            TitleAvailabilityPreferenceAdjuster(mutableMapOf<String, Double>("west" to 0.91, "central" to 1.02)),
            TieredAvailabilityPreferenceAdjuster(
                mutableListOf<Double>(3401.0, 3819.0),
                mutableListOf<Double>(0.81, 1.1),
            ),
            PercentAvailabilityPreferenceAdjuster(-6.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 28006.7,
    )
}
