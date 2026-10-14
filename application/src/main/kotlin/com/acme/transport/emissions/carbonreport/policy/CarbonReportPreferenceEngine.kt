package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the carbon report preference calculation. */
interface CarbonReportPreferenceFactor {
    fun describe(): String
    fun adjust(item: CarbonReportPreference, value: Double): Double
}

data class CapCarbonReportPreferenceAdjuster(var cap: Double) : CarbonReportPreferenceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CarbonReportPreference, value: Double): Double = minOf(value, cap)
}

data class LabelCarbonReportPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : CarbonReportPreferenceFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: CarbonReportPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCarbonReportPreferenceAdjuster(var percent: Double) : CarbonReportPreferenceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CarbonReportPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCarbonReportPreferenceAdjuster(var minimum: Double) : CarbonReportPreferenceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CarbonReportPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the carbon report preference steps in order and keeps a trace of each. */
data class CarbonReportPreferenceEngine(
    var steps: MutableList<CarbonReportPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CarbonReportPreference): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CarbonReportPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCarbonReportPreferenceEngine(): CarbonReportPreferenceEngine {
    return CarbonReportPreferenceEngine(
        steps = mutableListOf<CarbonReportPreferenceFactor>(
            CapCarbonReportPreferenceAdjuster(30148.0),
            LabelCarbonReportPreferenceAdjuster(mutableMapOf<String, Double>("retail" to 0.89, "online" to 1.4)),
            PercentCarbonReportPreferenceAdjuster(23.4),
            MinimumCarbonReportPreferenceAdjuster(12.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 50462.6,
    )
}
