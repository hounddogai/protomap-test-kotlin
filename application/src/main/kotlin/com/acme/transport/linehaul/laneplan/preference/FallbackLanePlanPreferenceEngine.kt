package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback lane plan preference calculation. */
interface FallbackLanePlanPreferenceModifier {
    fun describe(): String
    fun adjust(item: FallbackLanePlanPreference, value: Double): Double
}

data class RoundingFallbackLanePlanPreferenceAdjuster(var step: Double) : FallbackLanePlanPreferenceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackLanePlanPreference, value: Double): Double = roundTo(value, step)
}

data class FlaggedFallbackLanePlanPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackLanePlanPreferenceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackLanePlanPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryFallbackLanePlanPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackLanePlanPreferenceModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: FallbackLanePlanPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentFallbackLanePlanPreferenceAdjuster(var percent: Double) : FallbackLanePlanPreferenceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackLanePlanPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FallbackLanePlanPreferenceVersionOffsetAdjuster(
    var weight: Double,
) : FallbackLanePlanPreferenceModifier {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: FallbackLanePlanPreference, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class TieredFallbackLanePlanPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackLanePlanPreferenceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackLanePlanPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback lane plan preference steps in order and keeps a trace of each. */
data class FallbackLanePlanPreferenceEngine(
    var steps: MutableList<FallbackLanePlanPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackLanePlanPreference): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackLanePlanPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackLanePlanPreferenceEngine(flags: Registry): FallbackLanePlanPreferenceEngine {
    return FallbackLanePlanPreferenceEngine(
        steps = mutableListOf<FallbackLanePlanPreferenceModifier>(
            RoundingFallbackLanePlanPreferenceAdjuster(0.5),
            FlaggedFallbackLanePlanPreferenceAdjuster(flags, "fallback-lane-plan-preference-pending", -4.2),
            CategoryFallbackLanePlanPreferenceAdjuster(
                mutableMapOf<String, Double>("south" to 0.86, "east" to 0.73),
            ),
            PercentFallbackLanePlanPreferenceAdjuster(-12.1),
            FallbackLanePlanPreferenceVersionOffsetAdjuster(1.64),
            TieredFallbackLanePlanPreferenceAdjuster(
                mutableListOf<Double>(2327.0, 4381.0, 6246.0, 9111.0),
                mutableListOf<Double>(1.11, 1.02, 1.0, 0.9),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 8298.0,
    )
}
