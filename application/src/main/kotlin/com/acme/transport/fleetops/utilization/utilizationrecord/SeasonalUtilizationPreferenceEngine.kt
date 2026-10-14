package com.acme.transport.fleetops.utilization.utilizationrecord

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal utilization preference calculation. */
interface SeasonalUtilizationPreferenceModifier {
    fun describe(): String
    fun adjust(item: SeasonalUtilizationPreference, value: Double): Double
}

data class RoundingSeasonalUtilizationPreferenceAdjuster(
    var step: Double,
) : SeasonalUtilizationPreferenceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalUtilizationPreference, value: Double): Double = roundTo(value, step)
}

data class MinimumSeasonalUtilizationPreferenceAdjuster(
    var minimum: Double,
) : SeasonalUtilizationPreferenceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalUtilizationPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSeasonalUtilizationPreferenceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalUtilizationPreferenceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalUtilizationPreference, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal utilization preference steps in order and keeps a trace of each. */
data class SeasonalUtilizationPreferenceEngine(
    var steps: MutableList<SeasonalUtilizationPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalUtilizationPreference): Double {
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

    fun evaluateAll(items: MutableList<SeasonalUtilizationPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalUtilizationPreferenceEngine(flags: Registry): SeasonalUtilizationPreferenceEngine {
    return SeasonalUtilizationPreferenceEngine(
        steps = mutableListOf<SeasonalUtilizationPreferenceModifier>(
            RoundingSeasonalUtilizationPreferenceAdjuster(0.05),
            MinimumSeasonalUtilizationPreferenceAdjuster(26.0),
            FlaggedSeasonalUtilizationPreferenceAdjuster(flags, "seasonal-utilization-preference-primary", 6.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 60128.5,
    )
}
